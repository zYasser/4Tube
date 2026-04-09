package rabbitmq

import (
	"context"
	"encoder/internal/logging"
	"errors"
	"fmt"
	"os"

	"github.com/rabbitmq/amqp091-go"
	"gorm.io/gorm"
)

type RabbitConfig struct {
	Conn    *amqp091.Connection
	Channel *amqp091.Channel
}

func connectToRabbitMQ() (*RabbitConfig, error) {
	logger := logging.Component("rabbitmq")

	url := os.Getenv("RABBITMQ_URL")
	if url == "" {
		return nil, errors.New("RABBITMQ_URL is not set")
	}

	logger.Info("connecting to rabbitmq")
	conn, err := amqp091.Dial(url)
	if err != nil {
		return nil, fmt.Errorf("dial rabbitmq: %w", err)
	}

	ch, err := conn.Channel()
	if err != nil {
		conn.Close()
		return nil, fmt.Errorf("open rabbitmq channel: %w", err)
	}

	return &RabbitConfig{
		Conn:    conn,
		Channel: ch,
	}, nil
}

func (c *RabbitConfig) close() error {
	return errors.Join(c.Channel.Close(), c.Conn.Close())
}

func SetupRabbitMQ(ctx context.Context, db *gorm.DB) error {
	logger := logging.Component("rabbitmq")

	rabbitConfig, err := connectToRabbitMQ()
	if err != nil {
		return err
	}

	if err := rabbitConfig.consumeMessages("upload.queue", "upload_exchange", "direct", "upload.routing.key", db); err != nil {
		rabbitConfig.close()
		return err
	}

	go func() {
		<-ctx.Done()
		logger.Info("shutting down rabbitmq connection")
		if err := rabbitConfig.close(); err != nil {
			logger.Error("rabbitmq shutdown failed", "err", err)
		}
	}()

	logger.Info("rabbitmq consumer started", "queue", "upload.queue", "exchange", "upload_exchange")
	return nil
}
