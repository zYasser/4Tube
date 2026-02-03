package models

import (
	"time"

	"gorm.io/gorm"
)

type Job struct {
	gorm.Model
	Status       string    `gorm:"not null" default:"pending"`
	CreatedAt    time.Time `gorm:"not null" default:"current_timestamp"`
	UpdatedAt    time.Time `gorm:"not null" default:"current_timestamp"`
	CompletedAt  time.Time `gorm:"not null" default:"current_timestamp"`
	FailedAt     time.Time `gorm:"not null" default:"current_timestamp"`
	FailedReason string    `gorm:"not null" default:""`
	InputPath    string    `gorm:"not null" default:""`
	InputFormat  string    `gorm:"not null" default:""`
	Priority     int       `gorm:"not null" default:"0"`
}