package events

type UploadEvent struct {
	ID               string    `json:"id"`
	FileId           string `json:"fileId"`
	OriginalFilename string `json:"originalFilename"`
	Location         string `json:"location"`
	Size             int64  `json:"size"`
	ContentType      string `json:"contentType"`
	ChunkCount       int    `json:"chunkCount"`
}
