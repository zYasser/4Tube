package service

import (
	"encoder/internal/models"
	"encoder/pkg/rabbitmq"
	"gorm.io/gorm"
)

func CreateJob(event rabbitmq.UploadEvent, db *gorm.DB) error {

	job := models.BuildJob(event.ID, event.FileUrl)
	err := db.Create(job).Error
	if err != nil {
		return err
	}
	return nil
}
