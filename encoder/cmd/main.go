package main

import (
	"context"
	"encoder/internal/config"
	"encoder/internal/handlers"
	"encoder/rabbitmq"
	"fmt"
	"log"
	"net/http"

	"github.com/joho/godotenv"
)

func main() {
	err := godotenv.Load(".env")
	if err != nil {
		log.Fatal("Error loading .env file ", err)
	}
	ctx, cancel := context.WithCancel(context.Background())
	// Load configuration
	config := config.LoadConfig()

	// Setup routes
	router := handlers.SetupRoutes()

	err = rabbitmq.SetupRabbitMQ(ctx)
	if err != nil {
		log.Fatal(err)
	}


	defer cancel()
	
	// Start server
	addr := ":" + config.Port
	fmt.Printf("Server starting on %s...\n", addr)
	log.Fatal(http.ListenAndServe(addr, router))


}
