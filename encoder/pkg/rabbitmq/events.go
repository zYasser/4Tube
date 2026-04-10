package rabbitmq

import (
	"encoder/internal/events"
	"encoder/internal/logging"
	"encoder/internal/service"
	"encoding/json"
	"fmt"

	"github.com/rabbitmq/amqp091-go"
	"gorm.io/gorm"
)

func (con *RabbitConfig) consumeMessages(queueName string, exchangeName string, exchangeType string, routingKey string, db *gorm.DB) error {
	logger := logging.Component("rabbitmq-consumer").With(
		"queue", queueName,
		"exchange", exchangeName,
		"routing_key", routingKey,
	)

	err := con.Channel.ExchangeDeclare(
		exchangeName,
		exchangeType,
		true,
		false,
		false,
		false,
		nil,
	)
	if err != nil {
		return fmt.Errorf("declare exchange %s: %w", exchangeName, err)
	}

	q, err := con.Channel.QueueDeclare(
		queueName,
		true,
		false,
		false,
		false,
		amqp091.Table{
			"x-dead-letter-exchange":    "dlx-exchange",
			"x-dead-letter-routing-key": "upload.dlq"},
	)
	if err != nil {
		return fmt.Errorf("declare queue %s: %w", queueName, err)
	}

	err = con.Channel.QueueBind(
		queueName,
		routingKey,
		exchangeName,
		false,
		nil,
	)
	if err != nil {
		return fmt.Errorf("bind queue %s: %w", queueName, err)
	}
	msgs, err := con.Channel.Consume(
		q.Name,
		"go-encoder",
		false,
		false,
		false,
		false,
		nil,
	)
	if err != nil {
		return fmt.Errorf("start consumer for queue %s: %w", q.Name, err)
	}

	go func() {
		for d := range msgs {
			uploadEvent := events.UploadEvent{}
			if err := json.Unmarshal(d.Body, &uploadEvent); err != nil {
				logger.Error("failed to decode upload event", "delivery_tag", d.DeliveryTag, "err", err)
				if nackErr := d.Nack(false, false); nackErr != nil {
					logger.Error("failed to reject invalid message", "delivery_tag", d.DeliveryTag, "err", nackErr)
				}
				continue
			}

			logger.Info("upload event received",
				"delivery_tag", d.DeliveryTag,
				"file_id", uploadEvent.FileId,
				"original_filename", uploadEvent.OriginalFilename,
				"content_type", uploadEvent.ContentType,
				"chunk_count", uploadEvent.ChunkCount,
			)
			err := service.CreateJob(uploadEvent, db)
			if err != nil {
				logger.Error("Failed to process event %s , Error %v", string(uploadEvent.ID), err.Error())
				if d.Redelivered {
					d.Nack(false, false)

				} else {
					d.Nack(false, true)

				}
			}

			if err := d.Ack(false); err != nil {
				logger.Error("failed to acknowledge message", "delivery_tag", d.DeliveryTag, "err", err)
			}

		}

		logger.Warn("message consumer stopped")
	}()

	return nil
}
