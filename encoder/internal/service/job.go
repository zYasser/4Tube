package service

import (
	"encoder/internal/models"

	"gorm.io/gorm"
)

func CreateJob(job *models.Job, db *gorm.DB) (*models.Job, error) {
	err := db.Create(job).Error
	if err != nil {
		return nil, err
	}
	return job, nil
}