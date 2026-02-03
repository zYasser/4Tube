package rabbitmq

import (
	"encoding/json"
	"fmt"
	"log"

	"github.com/rabbitmq/amqp091-go"
	"gorm.io/gorm"
)

type UploadEvent struct {
	ID      string `json:"id"`
	FileUrl string `json:"file_url"`
}

func (con *RabbitConfig) consumeMessages(queueName string, exchangeName string, exchangeType string , routingKey string, db *gorm.DB) {

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
		log.Panicf("Failed to declare exchange: %v", err)
	}
	
	q, err := con.Channel.QueueDeclare(
		queueName,
		true,
		false, 
		false,  
		false, 
		amqp091.Table{}, 
	)
	if err != nil {
		log.Panicf("Failed to declare queue: %v", err)
	}

	err = con.Channel.QueueBind(
		queueName,
		routingKey,
		exchangeName,
		false,
		nil,
	)
	if err != nil {
		log.Panicf("Failed to declare queue: %v", err)
	}
	msgs, err := con.Channel.Consume(
		q.Name,
		"go-encoder",
		true,
		false,
		false,
		false,
		nil,
	)
	go func() {
			for d := range msgs {
				uploadEvent := UploadEvent{}
				json.Unmarshal(d.Body, &uploadEvent)
				fmt.Println("Upload event received", uploadEvent)

			}
	}()

}
