# Mobile Voice Stall

Android mobile adaptation of [Voice Stall](https://github.com/ChristianGonper/voice-stall) — speech-to-text with automatic clipboard pasting, powered by Groq cloud API.

## Features

- **Voice Recording** — Tap to record, tap to stop. Audio captured in AAC/m4a format at 16kHz.
- **Cloud Transcription** — Uses Groq API with OpenAI Whisper models (whisper-large-v3-turbo, whisper-large-v3, distil-whisper-large-v3-en).
- **Floating Overlay** — A draggable bubble that stays on top of any app with record and paste buttons.
- **Custom Dictionary** — Regex-based pattern replacement for correcting common transcription errors.
- **Transcription History** — Browse, copy, and delete past transcriptions.
- **Configurable** — Choose Whisper model, set language preference, manage API key.

## Architecture

- **Kotlin** + **Jetpack Compose** (Material Design 3)
- **MVVM** with Hilt dependency injection
- **Room** database for history and dictionary
- **DataStore** for preferences
- **Retrofit** + **OkHttp** for Groq API communication
- **Foreground Service** + **WindowManager** for floating overlay

## Setup

1. Clone the repository
2. Open in Android Studio
3. Build and run on device/emulator (min SDK 26 / Android 8.0)
4. Go to **Settings** and enter your [Groq API key](https://console.groq.com/)
5. Enable the floating overlay from the Home screen

## Permissions

- `RECORD_AUDIO` — Microphone access for recording
- `INTERNET` — Groq API calls
- `SYSTEM_ALERT_WINDOW` — Floating overlay
- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MICROPHONE` — Keep overlay service alive
- `POST_NOTIFICATIONS` — Notification for foreground service (Android 13+)

## Project Structure

```
app/src/main/java/com/voicestall/mobile/
├── audio/          # AudioRecorder (MediaRecorder wrapper)
├── overlay/        # OverlayService (floating bubble)
├── data/
│   ├── local/      # Room database, DAOs, entities
│   ├── remote/     # Groq API (Retrofit)
│   ├── preferences/# DataStore settings
│   └── repository/ # TranscriptionRepository, DictionaryRepository
├── di/             # Hilt DI module
├── ui/
│   ├── home/       # Main screen (record + overlay toggle)
│   ├── history/    # Transcription history
│   ├── dictionary/ # Custom dictionary management
│   ├── settings/   # API key, model, language config
│   ├── components/ # RecordButton, TranscriptionCard
│   ├── navigation/ # NavGraph + bottom nav
│   └── theme/      # Material 3 theme
└── util/           # DictionaryProcessor, ClipboardHelper
```
