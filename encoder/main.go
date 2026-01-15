package main

import (
	"fmt"
	"log"
	"net/http"
)

func main() {
	// Load configuration
	config := LoadConfig()

	// Setup routes
	router := SetupRoutes()

	// Start server
	addr := ":" + config.Port
	fmt.Printf("Server starting on %s...\n", addr)
	log.Fatal(http.ListenAndServe(addr, router))
}
