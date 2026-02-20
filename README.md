# PlantDiseaseDetection-Android
AI-powered Android application for detecting plant diseases from images and providing treatment and prevention recommendations.

# 🌿 PlantDiseaseDetection - AI-Powered Android App

An AI-powered Android application that detects plant diseases from images and provides treatment plans and prevention tips for farmers and gardeners.

## Features

- AI-based plant disease detection using images
- Upload images or provide image URLs for analysis
- Shows disease name and confidence percentage
- Provides treatment recommendations for detected diseases
- Prevention tips to avoid future infections
- Modern, user-friendly Android UI
- Clean architecture with Kotlin + XML
- Network integration with Retrofit
- Efficient image loading with Coil

# 📸 App Preview
### Home Screen
Displays the plant scan interface and AI analysis section.

### Detection Result
Shows detected disease name, confidence percentage, treatment plan, and prevention tips.

## 🛠 Tech Stack

- Kotlin
- XML UI
- Retrofit
- Coil Image Loader
- Android ViewBinding
- REST API
- AI Model Integration

## Project Structure
- PlantDiseaseDetection/
- │
- ├── app/
- │   ├── java/com/example/plantdiseasedetection/
- │   │   ├── ApiService.kt           # Retrofit API interface
- │   │   ├── RetrofitClient.kt       # Retrofit client setup
- │   │   ├── MainActivity.kt         # Main detection activity
- │   │   └── WelcomeActivity.kt      # Welcome screen activity
- │   │
- │   ├── res/
- │   │   ├── layout/
- │   │   │   ├── activity_main.xml
- │   │   │   └── activity_welcome.xml
- │   │   │
- │   │   ├── drawable/
- │   │   │   └── plant_logo.xml      # App logo or illustration
- │   │   │
- │   │   └── values/
- │   │       ├── colors.xml
- │   │       ├── strings.xml
- │   │       └── styles.xml
- │   │
- │   └── AndroidManifest.xml
- │
- └── build.gradle (app & project level)

- Kaggle Data Link: https://www.kaggle.com/datasets/vipoooool/new-plant-diseases-dataset                                            
- Kaggle Notebook: https://www.kaggle.com/code/ahmed1harfoush/plant-disease-detection-system
- colab Notebook: https://colab.research.google.com/drive/1HQ3jYlPFFFUAl37WfdAKx7sLUMpeMFXW?usp=sharing

![Plant Logo](https://media.springernature.com/lw685/springer-static/image/art%3A10.1007%2Fs40747-021-00536-1/MediaObjects/40747_2021_536_Fig2_HTML.jpg)

