package watcher


import (
	"log"
	"os"
	"path/filepath"
	"sync"
	"time"

	"github.com/fsnotify/fsnotify"
)

// FileWatcher watches a directory for new files
type FileWatcher struct {
	watchDir    string
	watcher     *fsnotify.Watcher
	onNewFile   func(string)
	knownFiles  map[string]bool
	mu          sync.RWMutex
	stopChan    chan struct{}
	isRunning   bool
}

// NewFileWatcher creates a new file watcher instance
func NewFileWatcher(watchDir string, onNewFile func(string)) (*FileWatcher, error) {
	// Normalize the directory path
	absPath, err := filepath.Abs(watchDir)
	if err != nil {
		return nil, err
	}

	// Check if directory exists
	if _, err := os.Stat(absPath); os.IsNotExist(err) {
		return nil, err
	}

	watcher, err := fsnotify.NewWatcher()
	if err != nil {
		return nil, err
	}

	fw := &FileWatcher{
		watchDir:   absPath,
		watcher:    watcher,
		onNewFile:  onNewFile,
		knownFiles: make(map[string]bool),
		stopChan:   make(chan struct{}),
		isRunning:  false,
	}

	// Initialize known files
	if err := fw.initializeKnownFiles(); err != nil {
		watcher.Close()
		return nil, err
	}

	return fw, nil
}

// initializeKnownFiles scans the directory and marks existing files as known
func (fw *FileWatcher) initializeKnownFiles() error {
	return filepath.Walk(fw.watchDir, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if !info.IsDir() {
			fw.mu.Lock()
			fw.knownFiles[path] = true
			fw.mu.Unlock()
		}
		return nil
	})
}


// Start begins watching the directory for new files
func (fw *FileWatcher) Start() error {
	fw.mu.Lock()
	if fw.isRunning {
		fw.mu.Unlock()
		return nil
	}
	fw.isRunning = true
	fw.mu.Unlock()

	// Add the directory to the watcher
	if err := fw.watcher.Add(fw.watchDir); err != nil {
		fw.mu.Lock()
		fw.isRunning = false
		fw.mu.Unlock()
		return err
	}

	// Start the watch loop in a goroutine
	go fw.watchLoop()

	log.Printf("File watcher started for directory: %s", fw.watchDir)
	return nil
}

// watchLoop processes file system events
func (fw *FileWatcher) watchLoop() {
	for {
		select {
		case event, ok := <-fw.watcher.Events:
			if !ok {
				return
			}

			// Only process create events for files (not directories)
			if event.Op&fsnotify.Create == fsnotify.Create {
				// Check if it's a file (not a directory)
				info, err := os.Stat(event.Name)
				if err != nil {
					continue
				}

				if !info.IsDir() {
					fw.mu.RLock()
					isNew := !fw.knownFiles[event.Name]
					fw.mu.RUnlock()

					if isNew {
						// Mark as known
						fw.mu.Lock()
						fw.knownFiles[event.Name] = true
						fw.mu.Unlock()

						// Call the callback
						if fw.onNewFile != nil {
							// Small delay to ensure file is fully written
							time.Sleep(100 * time.Millisecond)
							fw.onNewFile(event.Name)
						}
					}
				} else {
					// If a new directory is created, add it to the watcher
					fw.watcher.Add(event.Name)
				}
			}

		case err, ok := <-fw.watcher.Errors:
			if !ok {
				return
			}
			log.Printf("File watcher error: %v", err)

		case <-fw.stopChan:
			return
		}
	}
}

// Stop stops watching the directory
func (fw *FileWatcher) Stop() error {
	fw.mu.Lock()
	if !fw.isRunning {
		fw.mu.Unlock()
		return nil
	}
	fw.isRunning = false
	fw.mu.Unlock()

	close(fw.stopChan)
	return fw.watcher.Close()
}

// IsRunning returns whether the watcher is currently running
func (fw *FileWatcher) IsRunning() bool {
	fw.mu.RLock()
	defer fw.mu.RUnlock()
	return fw.isRunning
}

// GetWatchDir returns the directory being watched
func (fw *FileWatcher) GetWatchDir() string {
	return fw.watchDir
}
