package rabbitmq

import (
	"context"
	"errors"
	"log"
	"os"

	"github.com/rabbitmq/amqp091-go"
	"gorm.io/gorm"
)

type RabbitConfig struct {
	Conn    *amqp091.Connection
	Channel *amqp091.Channel
}

func connectToRabbitMQ() (*RabbitConfig, error) {
	log.Println("Connecting to RabbitMQ")
	url := os.Getenv("RABBITMQ_URL")
	if url == "" {
		return nil, errors.New("RABBITMQ_URL is not set")
	}
	conn, err := amqp091.Dial(url)
	if err != nil {
		log.Println("Failed to connect to RabbitMQ", err)
		return nil, err
	}

	ch, err := conn.Channel()
	if err != nil {
		return nil, err
	}

	return &RabbitConfig{
		Conn:    conn,
		Channel: ch,
	}, nil
}

func (c *RabbitConfig) close() error {
	return c.Conn.Close()
}


func SetupRabbitMQ(ctx context.Context , db *gorm.DB) ( error) {
	rabbitConfig, err := connectToRabbitMQ()
	if err != nil {
		return err
	}



	go rabbitConfig.consumeMessages("upload.queue", "upload_exchange", "direct", "upload.routing.key" , db)
	

	go func() {
		<-ctx.Done()
		log.Println("Stopping RabbitMQ connection")
		rabbitConfig.close()
	}()
	return nil
}

