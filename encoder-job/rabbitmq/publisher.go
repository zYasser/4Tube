package rabbitmq

import (
	"context"
	"encoding/json"
	"errors"
	"os"
	"time"

	"github.com/rabbitmq/amqp091-go"
)

const (
	StatusProcessing = "PROCESSING"
	StatusSuccess    = "SUCCESS"
	StatusFailed     = "FAILED"
)

type ChunkStatusEvent struct {
	JobID      string    `json:"job_id"`
	ChunkIndex int       `json:"chunk_index"`
	Status     string    `json:"status"`     // PROCESSING | SUCCESS | FAILED
	IsFinal    bool      `json:"isFinal"`    // matches requested casing
	UpdatedAt  time.Time `json:"updated_at"` // RFC3339 when encoded as JSON
}

type Publisher struct {
	conn       *amqp091.Connection
	ch         *amqp091.Channel
	exchange   string
	routingKey string
}

func NewPublisherFromEnv() (*Publisher, error) {
	url := os.Getenv("RABBITMQ_URL")
	if url == "" {
		return nil, errors.New("RABBITMQ_URL is not set")
	}

	exchange := os.Getenv("ENCODER_STATUS_EXCHANGE")
	if exchange == "" {
		exchange = "encoder.status.exchange"
	}

	routingKey := os.Getenv("ENCODER_STATUS_ROUTING_KEY")
	if routingKey == "" {
		routingKey = "encoder.status.routing.key"
	}

	conn, err := amqp091.Dial(url)
	if err != nil {
		return nil, err
	}

	ch, err := conn.Channel()
	if err != nil {
		_ = conn.Close()
		return nil, err
	}

	if err := ch.ExchangeDeclare(
		exchange,
		"direct",
		true,
		false,
		false,
		false,
		nil,
	); err != nil {
		_ = ch.Close()
		_ = conn.Close()
		return nil, err
	}

	return &Publisher{
		conn:       conn,
		ch:         ch,
		exchange:   exchange,
		routingKey: routingKey,
	}, nil
}

func (p *Publisher) Close() error {
	if p == nil {
		return nil
	}
	if p.ch != nil {
		_ = p.ch.Close()
	}
	if p.conn != nil {
		return p.conn.Close()
	}
	return nil
}

func (p *Publisher) PublishChunkStatus(ctx context.Context, ev ChunkStatusEvent) error {
	body, err := json.Marshal(ev)
	if err != nil {
		return err
	}

	return p.ch.PublishWithContext(
		ctx,
		p.exchange,
		p.routingKey,
		false,
		false,
		amqp091.Publishing{
			ContentType: "application/json",
			Timestamp:   time.Now(),
			Body:        body,
		},
	)
}

