# FinancialTracker

FinancialTracker is a comprehensive Android application designed to help users manage their personal finances, track expenses and income, and collaborate with friends.

## Features

- **User Authentication**: Secure login and registration using Firebase Auth.
- **Expense & Income Tracking**: Easily log and categorize your financial transactions.
- **Recurring Transactions**: Automated tracking for recurring income using Android WorkManager.
- **Data Visualization**: Gain insights into your spending habits with interactive charts powered by MPAndroidChart.
- **Social Integration**: 
    - Add and manage a friends list.
    - Real-time chat functionality.
    - Compare financial progress with friends.
- **Barcode/QR Scanning**: Integrated scanning capabilities for quick data entry or social features.
- **Media Support**: Profile and receipt image handling via Cloudinary and Glide.
- **Budgeting**: Set and monitor monthly budgets to stay on track.

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: XML Layouts with Material Components
- **Architecture**: MVVM (Model-View-ViewModel)
- **Backend**: 
    - [Firebase Authentication](https://firebase.google.com/docs/auth)
    - [Cloud Firestore](https://firebase.google.com/docs/firestore) & [Realtime Database](https://firebase.google.com/docs/database)
    - [Firebase Storage](https://firebase.google.com/docs/storage)
- **Libraries**:
    - [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) for data visualization.
    - [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for background tasks.
    - [CameraX](https://developer.android.com/training/camerax) for camera features.
    - [Glide](https://github.com/bumptech/glide) for image loading.
    - [Cloudinary](https://cloudinary.com/documentation/android_integration) for cloud-based image management.
    - [ZXing](https://github.com/zxing/zxing) for barcode scanning.

## Prerequisites

- Android Studio Ladybug (or newer)
- Android SDK Level 24+ (Min SDK)
- A Firebase Project

## Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/FinancialTracker.git
   ```

2. **Firebase Configuration**:
   - Create a new project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android App to your Firebase project with the package name `com.example.financialtracker`.
   - Download the `google-services.json` file and place it in the `app/` directory.

3. **API Keys**:
   - This project requires a Google API Key for certain services. 
   - Add your key to your `local.properties` file:
     ```properties
     GOOGLE_API_KEY=your_api_key_here
     ```

4. **Build the project**:
   - Open the project in Android Studio.
   - Sync Project with Gradle Files.
   - Run the application on an emulator or physical device.

## Project Structure

- `data/`: Contains models, repositories, and data helpers.
- `ui/`: Contains Activities, Fragments, and ViewModels.
- `workers/`: Background task implementations using WorkManager.
