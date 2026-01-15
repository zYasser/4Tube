package main

import (
	"encoder/internal/config"
	"encoder/internal/handlers"
	"fmt"
	"log"
	"net/http"
)

func main() {
	// Load configuration
	config := config.LoadConfig()

	// Setup routes
	router := handlers.SetupRoutes()

	// Start server
	addr := ":" + config.Port
	fmt.Printf("Server starting on %s...\n", addr)
	log.Fatal(http.ListenAndServe(addr, router))
}
