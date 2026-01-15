package main

import (
	"encoder/handlers"

	"github.com/gorilla/mux"
)

// SetupRoutes configures all application routes
func SetupRoutes() *mux.Router {
	r := mux.NewRouter()

	// API routes
	r.HandleFunc("/", handlers.HelloWorld).Methods("GET")

	return r
}
