from flask import Flask, request, jsonify
import numpy as np
import tensorflow as tf
from PIL import Image
import json
import requests
from io import BytesIO

app = Flask(__name__)

IMG_SIZE = 224

# Load TFLite model
interpreter = tf.lite.Interpreter(model_path="plant_disease_model.tflite")
interpreter.allocate_tensors()

input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

# Load mapping files
with open("class_names.json", "r") as f:
    class_names = json.load(f)

with open("plant_disease_treatment.json", "r") as f:
    disease_info = json.load(f)


def preprocess_image(image):
    image = image.resize((IMG_SIZE, IMG_SIZE))
    image = np.array(image, dtype=np.float32)
    image = np.expand_dims(image, axis=0)
    return image


def run_inference(image):
    input_data = preprocess_image(image)
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()

    output = interpreter.get_tensor(output_details[0]['index'])
    predicted_index = int(np.argmax(output))
    confidence = float(np.max(output))

    predicted_class = class_names[predicted_index]
    info = disease_info.get(predicted_class, {})

    return {
        "class": predicted_class,
        "disease": info.get("disease_name", "Unknown"),
        "confidence": confidence,
        "treatment": info.get("treatment", "No treatment info available."),
        "prevention": info.get("prevention", "No prevention info available."),
        "notes": info.get("notes", "")
    }


# 1. Handle Local File Uploads
@app.route("/predict", methods=["POST"])
def predict():
    if "image" not in request.files:
        return jsonify({"error": "No image uploaded"}), 400

    file = request.files["image"]
    image = Image.open(file.stream).convert("RGB")
    return jsonify(run_inference(image))


# 2. Handle Image URLs (Fixes the 404 error)
@app.route("/predict-url", methods=["POST"])
def predict_url():
    url = request.form.get("url")
    if not url:
        return jsonify({"error": "No URL provided"}), 400

    try:
        response = requests.get(url)
        image = Image.open(BytesIO(response.content)).convert("RGB")
        return jsonify(run_inference(image))
    except Exception as e:
        return jsonify({"error": f"Failed to load image from URL: {str(e)}"}), 500


@app.route("/")
def home():
    return "Plant Disease Detection API Running"


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)