package main

import (
	"context"
	"encoder/internal/config"
	"encoder/internal/handlers"
	"encoder/internal/logging"
	"encoder/pkg/rabbitmq"
	"errors"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/joho/godotenv"
)

func main() {
	logger := logging.Setup().With("component", "startup")

	if err := godotenv.Load(".env"); err != nil {
		logger.Warn(".env file was not loaded", "path", ".env", "err", err)
	}
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	appConfig := config.LoadConfig()
	logger.Info("configuration loaded", "port", appConfig.Port)

	db, err := config.SetupDatabase()
	if err != nil {
		logger.Error("database initialization failed", "err", err)
		return
	}
	logger.Info("database initialized")

	app := &config.Application{
		DB:     db,
		Router: mux.NewRouter(),
	}

	app.Router = handlers.SetupRoutes()
	logger.Info("routes initialized")

	if err := rabbitmq.SetupRabbitMQ(ctx, app.DB); err != nil {
		logger.Error("rabbitmq initialization failed", "err", err)
		return
	}
	addr := ":" + appConfig.Port
	logger.Info("http server starting", "addr", addr)

	if err := http.ListenAndServe(addr, app.Router); err != nil && !errors.Is(err, http.ErrServerClosed) {
		logger.Error("http server stopped unexpectedly", "addr", addr, "err", err)
	}

}
