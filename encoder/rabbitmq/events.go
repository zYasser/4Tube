package rabbitmq

import (
	"log"

	"github.com/rabbitmq/amqp091-go"
)

type UploadEvent struct {
	ID      string `json:"id"`
	Message string `json:"message"`
}

func (con *RabbitConfig) ConsumeMessages(queueName string, exchangeName string, exchangeType string , routingKey string) {

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
		true,  
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
			log.Printf("Received a message: %s", d.Body)
		}
	}()

}
