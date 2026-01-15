package handlers

import (
	"fmt"
	"net/http"
)

// HelloWorld handles the root endpoint
func HelloWorld(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "Hello World!")
}
