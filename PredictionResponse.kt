package com.example.plantdiseasedetection

data class PredictionResponse(
    val class_name: String,
    val disease: String,
    val confidence: Double,
    val treatment: String,
    val prevention: String,
    val notes: String
)