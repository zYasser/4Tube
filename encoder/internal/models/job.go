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
	Priority     int       `gorm:"not null" default:"0"`
	SourceJobId  string    `gorm : ""`
}

func BuildJob(id, filepath string) *Job {
	return &Job{
		InputPath:   filepath,
	}

}

func BuildSubJob(id, filepath string) *Job {
	return &Job{
		SourceJobId: id,
		InputPath:   filepath,
	}

}

