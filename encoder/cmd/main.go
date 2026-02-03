package main

import (
	"context"
	"encoder/internal/config"
	"encoder/internal/handlers"
	"encoder/rabbitmq"
	"fmt"
	"log"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/joho/godotenv"
)



func main() {
	err := godotenv.Load(".env")
	if err != nil {
		log.Fatal("Error loading .env file ", err)
	}
	ctx, cancel := context.WithCancel(context.Background())

	// Setup database
	db := config.SetupDatabase()
	// Load configuration
	appConfig := config.LoadConfig()

	app := &config.Application{
		DB: db,
		Router: mux.NewRouter(),
	}

	// Setup routes
	app.Router = handlers.SetupRoutes()

	err = rabbitmq.SetupRabbitMQ(ctx, app.DB)
	if err != nil {
		log.Fatal(err)
	}


	defer cancel()
	
	// Start server
	addr := ":" + appConfig.Port
	fmt.Printf("Server starting on %s...\n", addr)
	log.Fatal(http.ListenAndServe(addr, app.Router))

}
