# File Watcher

A Go package for watching directories and detecting new files.

## Features

- Watch a directory for new file creation events
- Automatically track known files to only report new ones
- Thread-safe implementation
- Easy to use callback mechanism
- Handles subdirectories automatically

## Installation

```bash
go mod tidy
```

## Usage

### Basic Example

```go
package main

import (
    "fmt"
    "log"
    "time"
    
    "job-encoder/watcher"
)

func main() {
    // Define the directory to watch
    watchDir := "./watch_folder"
    
    // Define callback for new files
    onNewFile := func(filePath string) {
        fmt.Printf("New file detected: %s\n", filePath)
        // Process the new file here
    }
    
    // Create and start the watcher
    fw, err := watcher.NewFileWatcher(watchDir, onNewFile)
    if err != nil {
        log.Fatal(err)
    }
    
    if err := fw.Start(); err != nil {
        log.Fatal(err)
    }
    
    // Keep running
    for {
        time.Sleep(1 * time.Second)
    }
}
```

### API

#### `NewFileWatcher(watchDir string, onNewFile func(string)) (*FileWatcher, error)`
Creates a new file watcher instance for the specified directory.

#### `Start() error`
Starts watching the directory for new files.

#### `Stop() error`
Stops watching and cleans up resources.

#### `IsRunning() bool`
Returns whether the watcher is currently running.

#### `GetWatchDir() string`
Returns the directory path being watched.

## Notes

- The watcher only reports files that are created after it starts (existing files are ignored)
- A small delay (100ms) is added after file creation to ensure the file is fully written
- The watcher automatically watches subdirectories when they are created
