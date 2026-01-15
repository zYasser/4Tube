# Encoder

A simple Go web server using Gorilla Mux.

## Project Structure

```
encoder/
├── handlers/           # HTTP request handlers
│   ├── hello.go       # Hello World handler
│   └── hello_test.go  # Handler tests
├── config.go          # Application configuration
├── routes.go          # Route definitions
├── main.go            # Application entry point
├── go.mod
└── README.md
```

## Features

- Clean separation of concerns
- HTTP handlers in dedicated package
- Configurable port via environment variable
- Unit tests for handlers
- Uses Gorilla Mux for routing

## Running the Application

```bash
# Run the server
go run .

# The server will start on port 8081 by default
# Visit http://localhost:8081/ to see "Hello World!"

# Or set a custom port
PORT=3000 go run .
```

## Running Tests

```bash
go test ./...
```

## Configuration

- `PORT`: Server port (default: 8081)

