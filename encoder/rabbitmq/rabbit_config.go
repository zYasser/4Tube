package rabbitmq

import (
	"os"

	"github.com/rabbitmq/amqp091-go"
)

type RabbitConfig struct {
	Conn    *amqp091.Connection
	Channel *amqp091.Channel
}

func ConnectToRabbitMQ() (*RabbitConfig, error) {

	conn, err := amqp091.Dial(os.Getenv("RABBITMQ_URL"))
	if err != nil {
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

func (c *RabbitConfig) Close() error {
	return c.Conn.Close()
}

