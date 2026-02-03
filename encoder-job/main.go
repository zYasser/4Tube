package main

import (
	"context"
	"fmt"
	"job-encoder/ffmpeg"
	"job-encoder/rabbitmq"
	"job-encoder/watcher"
	"log"
	"os"
	"path/filepath"
	"regexp"
	"strconv"
	"strings"
	"sync/atomic"
	"time"

	"github.com/joho/godotenv"
)

type JobEncoder struct {
	JobID string
	
}

func main() {
	// Load .env file
	if err := godotenv.Load(); err != nil {
		log.Printf("Warning: Error loading .env file: %v", err)
	}

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	// Get watch directory from environment variable, default to ./watch_folder
	watchDir := os.Getenv("WATCH_DIR")
	if watchDir == "" {
		watchDir = "./watch_folder"
	}

	pub, err := rabbitmq.NewPublisherFromEnv()
	if err != nil {
		log.Fatalf("Failed to setup RabbitMQ publisher: %v", err)
	}
	defer func() {
		if err := pub.Close(); err != nil {
			log.Printf("Warning: failed to close RabbitMQ publisher: %v", err)
		}
	}()

	jobID := os.Getenv("JOB_ID")
	if jobID == "" {
		// best-effort fallback (keeps events non-empty during local runs)
		out := os.Getenv("FFMPEG_OUTPUT_FILE")
		if out != "" {
			base := filepath.Base(out)
			jobID = strings.TrimSuffix(base, filepath.Ext(base))
		}
		if jobID == "" {
			jobID = "unknown"
		}
	}

	var lastChunkIndex int64 = -1
	chunkIndexRe := regexp.MustCompile(`_(\d+)\.ts$`)

	// Define callback function for when new files are detected
	onNewFile := func(filePath string) {
		fmt.Printf("New file detected: %s\n", filePath)
		if strings.ToLower(filepath.Ext(filePath)) != ".ts" {
			return
		}

		m := chunkIndexRe.FindStringSubmatch(filepath.Base(filePath))
		if len(m) != 2 {
			return
		}

		idx, err := strconv.Atoi(m[1])
		if err != nil {
			return
		}

		atomic.StoreInt64(&lastChunkIndex, int64(idx))

		_ = pub.PublishChunkStatus(ctx, rabbitmq.ChunkStatusEvent{
			JobID:      jobID,
			ChunkIndex: idx,
			Status:     rabbitmq.StatusProcessing,
			IsFinal:    false,
			UpdatedAt:  time.Now().UTC(),
		})
	}

	// Create a new file watcher
	fw, err := watcher.NewFileWatcher(watchDir, onNewFile)
	if err != nil {
		log.Fatalf("Failed to create file watcher: %v", err)
	}

	// Start watching
	if err := fw.Start(); err != nil {
		log.Fatalf("Failed to start file watcher: %v", err)
	}

	fmt.Printf("Watching directory: %s\n", fw.GetWatchDir())
	fmt.Println("Press Ctrl+C to stop...")
	ff := ffmpeg.NewFfmpegJob()

	// optional "job started" event
	_ = pub.PublishChunkStatus(ctx, rabbitmq.ChunkStatusEvent{
		JobID:      jobID,
		ChunkIndex: int(atomic.LoadInt64(&lastChunkIndex)),
		Status:     rabbitmq.StatusProcessing,
		IsFinal:    false,
		UpdatedAt:  time.Now().UTC(),
	})

	if err := ff.Run(); err != nil {
		_ = pub.PublishChunkStatus(ctx, rabbitmq.ChunkStatusEvent{
			JobID:      jobID,
			ChunkIndex: int(atomic.LoadInt64(&lastChunkIndex)),
			Status:     rabbitmq.StatusFailed,
			IsFinal:    true,
			UpdatedAt:  time.Now().UTC(),
		})
		log.Fatalf("Failed to start FFmpeg job: %v", err)
	}

	select {
	case ffErr := <-ff.FinishedChan:
		time.Sleep(1000 * time.Millisecond)
		fw.Stop()
		if ffErr != nil {
			_ = pub.PublishChunkStatus(ctx, rabbitmq.ChunkStatusEvent{
				JobID:      jobID,
				ChunkIndex: int(atomic.LoadInt64(&lastChunkIndex)),
				Status:     rabbitmq.StatusFailed,
				IsFinal:    true,
				UpdatedAt:  time.Now().UTC(),
			})
			fmt.Printf("FFmpeg job finished with error: %v\n", ffErr)
			os.Exit(1)
		}

		_ = pub.PublishChunkStatus(ctx, rabbitmq.ChunkStatusEvent{
			JobID:      jobID,
			ChunkIndex: int(atomic.LoadInt64(&lastChunkIndex)),
			Status:     rabbitmq.StatusSuccess,
			IsFinal:    true,
			UpdatedAt:  time.Now().UTC(),
		})
		fmt.Println("FFmpeg job finished")
		os.Exit(0)
	}
}
