package service

import (
	"encoder/internal/models"
	"encoder/pkg/rabbitmq"
	"fmt"

	"gorm.io/gorm"
)

func CreateJob(event rabbitmq.UploadEvent, db *gorm.DB) error {
    tx := db.Begin()
    if tx.Error != nil {
        return fmt.Errorf("failed to begin transaction: %w", tx.Error)
    }

    for i := 0; i < event.ChunkCount; i++ {
        job := models.BuildJob(string(event.ID), event.FileId)
        if result := tx.Create(&job); result.Error != nil {
            tx.Rollback()
            return fmt.Errorf("failed to create job record at chunk %d: %w", i, result.Error)
        }
    }

    if err := tx.Commit().Error; err != nil {
        tx.Rollback()
        return fmt.Errorf("failed to commit transaction: %w", err)
    }

    return nil
}