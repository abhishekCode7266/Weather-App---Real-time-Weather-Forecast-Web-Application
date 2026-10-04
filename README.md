# 🌤️ Real-time Weather Forecast Application (Web & Android)

A modern, responsive, and real-time Weather Forecast Application built using **Flutter (Web & Android APK)**.

---

## 🚀 Live Demo & APK Download

- 🌐 **Live Web Application (GitHub Pages)**:  
  👉 **[https://abhishekcode7266.github.io/Weather-App---Real-time-Weather-Forecast-Web-Application/](https://abhishekcode7266.github.io/Weather-App---Real-time-Weather-Forecast-Web-Application/)**

- 📱 **Android APK**:  
  You can download the latest release APK (`app-release.apk`) directly from the **[Releases](https://github.com/abhishekCode7266/Weather-App---Real-time-Weather-Forecast-Web-Application/releases)** section or the **GitHub Actions Artifacts**!

---

## ✨ Features

- ⚡ **Zero-Setup Real-time Weather**: Powered by high-reliability Open-Meteo REST API (works instantly out-of-the-box without requiring an API key).
- 🔑 **Optional OpenWeatherMap Support**: Users can enter their personal OpenWeatherMap API key in settings if desired.
- 🕒 **24-Hour Hourly Forecast**: Hourly temperature and condition progression over the next 24 hours.
- 📅 **7-Day Extended Forecast**: Daily minimum/maximum temperatures, weather conditions, and visual temperature bars.
- 🔍 **Global City Search & Quick Chips**: Search any city in the world (e.g., Delhi, Mumbai, London, New York, Tokyo, Paris) with quick-access chips.
- 🌡️ **Temperature Unit Toggle**: Seamlessly switch between Celsius (°C) and Fahrenheit (°F).
- 💾 **Local Persistence**: Saves your last searched city and unit preferences via `shared_preferences`.
- 🎨 **Adaptive Glassmorphic Material 3 UI**: Dynamic background gradients that change according to condition (Sunny/Clear, Rainy, Cloudy, Thunderstorm, Snow) and Day/Night.
- 📱 **Cross-Platform**: Runs flawlessly on mobile (Android APK) and desktop web browsers without blank/white screens.

---

## 🛠️ Project Structure

```text
├── .github/
│   └── workflows/
│       └── deploy.yml          # Automated CI/CD for Flutter Web & Android APK
├── android/                    # Native Android wrapper for APK generation
│   ├── app/
│   │   ├── build.gradle
│   │   └── src/main/AndroidManifest.xml
│   ├── build.gradle
│   └── settings.gradle
├── web/                        # Flutter Web configuration
│   ├── index.html              # Custom loader to prevent white screen & base-href handler
│   ├── manifest.json
│   └── favicon.png
├── lib/
│   ├── main.dart               # Flutter app entry point
│   ├── models/
│   │   └── weather_model.dart  # Data model for Open-Meteo & OpenWeatherMap
│   ├── services/
│   │   ├── weather_service.dart # HTTP REST client (web-safe, no dart:io)
│   │   └── storage_service.dart # SharedPreferences persistence
│   ├── screens/
│   │   └── weather_home_screen.dart # Main weather dashboard
│   └── widgets/
│       ├── current_weather_card.dart # Glassmorphism weather summary card
│       ├── hourly_forecast_widget.dart # 24h hourly forecast scroll
│       ├── daily_forecast_widget.dart  # 7-day forecast list
│       ├── weather_header.dart         # Search bar & quick city chips
│       └── settings_dialog.dart        # Unit & API key settings
└── pubspec.yaml                # Flutter project configuration
```

---

## 💻 Running Locally

### Prerequisites
- [Flutter SDK](https://flutter.dev/docs/get-started/install) (3.24+ recommended)

### Commands
```bash
# 1. Clone repository
git clone https://github.com/abhishekCode7266/Weather-App---Real-time-Weather-Forecast-Web-Application.git
cd Weather-App---Real-time-Weather-Forecast-Web-Application

# 2. Install dependencies
flutter pub get

# 3. Run on Web
flutter run -d chrome

# 4. Build Web for Production
flutter build web --release --base-href "/Weather-App---Real-time-Weather-Forecast-Web-Application/"

# 5. Build Android Release APK
flutter build apk --release
```

The compiled APK will be located at:
`build/app/outputs/flutter-apk/app-release.apk`
