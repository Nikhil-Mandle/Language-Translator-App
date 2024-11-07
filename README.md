# Language Translator App
This application translates text and audio from English to Hindi using Google's ML Kit. The app provides both text input and audio speech recognition for translation.

## Features

- **Text Translation**: Translate typed English text into Hindi.
- **Speech Recognition**: Recognize English speech and translate it into Hindi text.
- **ML Kit Translation**: Utilizes Google’s ML Kit for on-device language translation.

- ## Dependencies

Add the ML Kit Translate dependency in your `build.gradle` file:

``` implementation(libs.translate) ```

toml file
```
[versions]
translate = "17.0.3"

[libraries]
translate = { module = "com.google.mlkit:translate", version.ref = "translate" }
```
## Permissions
```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```
## Installation
1. Clone the repository:
git clone https://github.com/Nikhil-Mandle/Language-Translator-App.git
2. Open the project in Android Studio.
3. Sync the project to download dependencies.

## Usage
1. Text Translation:
- Type text in English and press the "Translate" button to see the Hindi translation.
2. Speech Translation:
- Press the "Mic" button to start speech recognition.
- Speak in English, and the app will display the recognized text along with its Hindi translation.
