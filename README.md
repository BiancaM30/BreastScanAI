# BreastScanAI: Dual Intelligent System for Breast Cancer Diagnosis

**BreastScanAI** is an AI-driven mobile application designed to assist in the early detection and diagnosis of breast cancer. This app leverages deep learning models to analyze mammograms and ultrasounds, providing users with segmentation and classification of potential breast tumors.

## 📽️ Video Demo

You can click below to download the video demo:

<a href="https://github.com/BiancaM30/BreastScanAI/blob/main/Demo-BreastScanAI.mp4">
    <img src="https://github.com/BiancaM30/BreastScanAI/blob/main/app_screenshot.jpg" alt="App Demo" width="300" height="auto">
</a>

## 🌟 Features
- **Dual AI Models**: Uses convolutional neural networks (CNN) for both **mammography** and **ultrasound** image analysis.
- **Tumor Segmentation**: Employs the U-Net architecture and YOLOv8x for tumor segmentation in medical images.
- **Tumor Classification**: Utilizes a custom CNN to classify tumors as benign or malignant.
- **Data Augmentation**: Uses GANs (Generative Adversarial Networks) for synthetic image generation to augment ultrasound data.
- **Real-time Analysis**: Provides results for classification and segmentation within seconds on mobile devices.
- **Integrated into Android**: The app allows users to upload medical images from their devices and instantly get predictions.
- **Offline and Online Support**: Capable of functioning both offline and synchronizing with the Flask backend server when online.

## 📊 AI Models and Technologies
- **YOLOv8x** for instance segmentation of mammograms.
- **U-Net Architecture** for ultrasound tumor segmentation.
- **CNN Model** for the classification of both mammograms and ultrasounds.
- **Flask API** serving as the backend for data processing and model inference.


## ⚙️ How It Works
1. **Upload Medical Image**: The user uploads a mammogram or ultrasound image via the Android app.
2. **AI Analysis**: The app preprocesses the image and sends it to the Flask server for segmentation and classification using AI models.
3. **Result Visualization**: The results, including tumor masks and bounding boxes, are displayed to the user.

## 🔍 Key Contributions
- **Innovative Dual System**: Combines both segmentation and classification in one app for mammograms and ultrasounds.
- **Pectoral Muscle Removal**: Investigated the impact of pectoral muscle removal on mammogram image analysis.
- **End-to-End System**: Developed a comprehensive end-to-end solution with a seamless user interface for both professionals and general users.




