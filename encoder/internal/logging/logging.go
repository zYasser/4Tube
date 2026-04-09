package logging

import (
	"log/slog"
	"os"
)

const serviceName = "encoder"

// Setup installs a shared structured logger for the application.
func Setup() *slog.Logger {
	logger := slog.New(slog.NewTextHandler(os.Stdout, &slog.HandlerOptions{
		AddSource: true,
		Level:     slog.LevelInfo,
	})).With("service", serviceName)

	slog.SetDefault(logger)
	return logger
}

func Component(name string) *slog.Logger {
	return slog.Default().With("component", name)
}
