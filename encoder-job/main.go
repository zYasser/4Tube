package main

import (
	"fmt"
	"job-encoder/ffmpeg"
	"job-encoder/watcher"
	"log"
	"os"
	"time"

	"github.com/joho/godotenv"
)

func main() {
	// Load .env file
	if err := godotenv.Load(); err != nil {
		log.Printf("Warning: Error loading .env file: %v", err)
	}

	// Get watch directory from environment variable, default to ./watch_folder
	watchDir := os.Getenv("WATCH_DIR")
	if watchDir == "" {
		watchDir = "./watch_folder"
	}

	// Define callback function for when new files are detected
	onNewFile := func(filePath string) {
		fmt.Printf("New file detected: %s\n", filePath)
		// Add your file processing logic here
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
	ffmpeg:= ffmpeg.NewFfmpegJob()
	ffmpeg.Run()

	select {
	case <-ffmpeg.FinishedChan:
		time.Sleep(1000 * time.Millisecond)
		fw.Stop()
		fmt.Println("FFmpeg job finished")
		os.Exit(0)
	}
}
