package main

import "os"

// Config holds application configuration
type Config struct {
	Port string
}

// LoadConfig loads configuration from environment variables with defaults
func LoadConfig() *Config {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8081"
	}

	return &Config{
		Port: port,
	}
}
