package handlers

import (
	"github.com/gorilla/mux"
)

// SetupRoutes configures all application routes
func SetupRoutes() *mux.Router {
	r := mux.NewRouter()

	// API routes
	r.HandleFunc("/", HelloWorld).Methods("GET")

	return r
}
