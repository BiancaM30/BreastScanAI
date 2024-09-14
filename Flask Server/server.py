import cv2
import numpy as np
from ultralytics import YOLO
from PIL import Image
from flask import Flask, request, jsonify
import base64
import io

app = Flask(__name__)
model = YOLO('models/best.pt')
model.conf = 0.1
class_names = ["benign", "malignant"]


@app.route('/')
def home():
    return "Hello, this is your Flask server!"


@app.route('/predict', methods=['POST'])
def predict():
    print("Request received.")
    data = request.json
    img_data = base64.b64decode(data['img'])
    img = Image.open(io.BytesIO(img_data))

    # convert the image to numpy array and then to grayscale
    img_np = np.array(img)
    img_gray = cv2.cvtColor(img_np, cv2.COLOR_BGR2GRAY)
    img_gray = np.stack((img_gray,) * 3, axis=-1)  # Stack grayscale image to mimic RGB

    model.conf = 0.1  # set the confidence threshold to 0.1
    class_names = ["benign", "malignant"]

    # Predict with the model
    results = model(img_gray)  # predict on an image

    response = []

    for result in results:
        boxes = result.boxes  # Boxes object for bbox outputs
        masks = result.masks  # Masks object for segmentation masks outputs
        class_ids = boxes.cls  # Class IDs for bounding boxes

        # Find the class label
        predicted_labels = []
        for class_id in class_ids:
            predicted_labels.append(class_names[int(class_id)])
        # Determine the final label based on the predicted labels
        predicted_label = "benign" if "malignant" not in predicted_labels else "malignant"
        print("Predicted label: ", predicted_label)

        # Find the binary segmentation mask
        mask_data = masks.data.cpu().numpy()  # Convert mask data to numpy arrays
        binary_mask = np.zeros((img_gray.shape[0], img_gray.shape[1], 3),
                               dtype=np.uint8)  # Create a black image with the same size as the original image and 3 channels
        for mask in mask_data:
            mask = cv2.resize(mask, (img_gray.shape[1], img_gray.shape[0]))  # Resize mask to match image size
            mask = (mask * 255).astype(np.uint8)  # Convert mask to 8-bit grayscale image
            _, mask_bin = cv2.threshold(mask, 127, 255, cv2.THRESH_BINARY)  # Apply binary threshold to the mask
            binary_mask[np.where(mask_bin == 255)] = (
                255, 255, 255)  # Apply the binary mask to the black background, setting tumor pixels to white
        # convert numpy array to bytes and then encode it into base64 string
        binary_mask_bytes = cv2.imencode('.jpg', binary_mask)[1].tobytes()
        binary_mask_b64 = base64.b64encode(binary_mask_bytes).decode('utf-8')

        # Find bounding box
        bounding_boxes = []
        for box in boxes.xyxy.tolist():
            x1, y1, x2, y2 = box  # Now you should be able to unpack the array
            bounding_boxes.append({"x1": int(x1), "y1": int(y1), "x2": int(x2), "y2": int(y2)})

        response.append({
            "label": predicted_label,
            "mask": binary_mask_b64,
            "bounding_boxes": bounding_boxes
        })

    return jsonify(response)


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
