package ffmpeg

import (
	"fmt"
	"os"
	"os/exec"
	"strings"
)

type FfmpegJob struct {
	InputFile       string
	OutputFile      string
	AudioBitrate    string
	VideoBitrate    string
	VideoCodec      string
	AudioCodec      string
	VideoResolution string
	StartTime       string
	OutputFormat    string
	FinishedChan    chan bool
}

// InitFromEnv initializes FfmpegJob from environment variables
func NewFfmpegJob() *FfmpegJob {
	return &FfmpegJob{
		InputFile:       os.Getenv("FFMPEG_INPUT_FILE"),
		OutputFile:      os.Getenv("FFMPEG_OUTPUT_FILE"),
		AudioBitrate:    os.Getenv("FFMPEG_AUDIO_BITRATE"),
		VideoBitrate:    os.Getenv("FFMPEG_VIDEO_BITRATE"),
		VideoCodec:      os.Getenv("FFMPEG_VIDEO_CODEC"),
		AudioCodec:      os.Getenv("FFMPEG_AUDIO_CODEC"),
		VideoResolution: os.Getenv("FFMPEG_VIDEO_RESOLUTION"),
		StartTime:       os.Getenv("FFMPEG_START_TIME"),
		OutputFormat:    os.Getenv("FFMPEG_OUTPUT_FORMAT"),
		FinishedChan:    make(chan bool),
	}
}
func (job *FfmpegJob) Run() error {
    command := job.buildArgs()
    fmt.Println("Running command:", strings.Join(command, " "))

    cmd := exec.Command("ffmpeg", command...)
    
    cmd.Stdout = os.Stdout
    cmd.Stderr = os.Stderr

    if err := cmd.Start(); err != nil {
        return fmt.Errorf("failed to start ffmpeg: %w", err)
    }

    go func() {
        if err := cmd.Wait(); err != nil {
            fmt.Printf("ffmpeg finished with error: %v\n", err)
        }
        job.FinishedChan <- true
    }()

    return nil
}
func (job *FfmpegJob) buildArgs() []string {
    args := []string{}
    if job.StartTime != "" {
        args = append(args, "-ss", job.StartTime)
    }
    args = append(args,
        "-i", job.InputFile,
        "-c:v", job.VideoCodec,
        "-c:a", job.AudioCodec,
        "-b:v", job.VideoBitrate,
        "-b:a", job.AudioBitrate,
        "-s", job.VideoResolution,
        "-f", "hls",
        "-hls_time", "4",
        "-hls_list_size", "0",
        "-hls_segment_filename", job.OutputFile + "_%03d.ts",
        "-hls_playlist_type", "vod",
        job.OutputFile,
    )
    return args
}
