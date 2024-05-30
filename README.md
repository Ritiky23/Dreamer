# Dreamer

Hi Team,

I hope this message finds you well.

I am pleased to inform you that I have successfully completed the Week 1 task for Chamberly AB. I have developed a Kotlin application named "Dreamer" using Firebase as the backend. Below are the key features and functionalities of the app:

## Key Features

1. **Dream Interpretation**
   - The app takes user-inputted dreams and provides probable meanings related to the user's subconscious mind. For this, I have integrated the Gemini API.

2. **User Profile Management**
   - User profile information, including profile pictures, names, and phone numbers, is stored using Firestore.

3. **Real-Time Dream Input and Response History**
   - To ensure a seamless user experience, I have used Firestore to fetch real-time dream inputs and responses, which are displayed in a RecyclerView list.

4. **Firestore Storage**
   - I have utilized Firestore Storage to store the references for profile pictures, names, and phone numbers.

5. **Cache Optimization**
   - For improved performance and user experience, I have implemented a cache optimization strategy using Firebase Realtime Database.

6. **Local Data Storage**
   - The app uses Room Database to store user data locally, enhancing the overall user experience by enabling offline access and faster data retrieval.

## Getting Started

### Prerequisites

- Kotlin
- Firebase Account
- Gemini API Key

### Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/yourusername/dreamer.git
    ```
2. Open the project in Android Studio.
3. Set up Firebase:
    - Add the `google-services.json` file to the `app` directory.
    - Enable Firestore, Firebase Realtime Database, and Firebase Storage in the Firebase Console.
4. Set up Gemini API:
    - Obtain a Gemini API key and add it to your project.

5. Build and run the app on your Android device or emulator.

## Usage

1. **Sign Up / Log In:**
   - Users can sign up or log in using their email and password.
   
2. **Profile Management:**
   - Users can update their profile information, including uploading a profile picture.

3. **Input Dreams:**
   - Users can input their dreams and receive interpretations based on the Gemini API.

4. **View History:**
   - Users can view their past dream inputs and interpretations in a RecyclerView list.



