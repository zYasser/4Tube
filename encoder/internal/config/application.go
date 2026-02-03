package config

import (
	"github.com/gorilla/mux"
	"gorm.io/gorm"
)

type Application struct {
	DB *gorm.DB
	Router *mux.Router
}