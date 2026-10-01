package com.example.ui.fluttercode

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FlutterCodeFile(
    val title: String,
    val filePath: String,
    val language: String,
    val content: String
)

@Composable
fun FlutterCodeHubScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFileIndex by remember { mutableStateOf(0) }

    val flutterFiles = remember { getFlutterCodeFiles() }
    val activeFile = flutterFiles[selectedFileIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Flutter Code & Project Lab",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Assignment 1 (Weather App) Flutter Source Code",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Copy Button
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(activeFile.filePath, activeFile.content)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied ${activeFile.title} to clipboard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("copy_flutter_code_button")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy File")
            }
        }

        // Tabs to switch Flutter files
        ScrollableTabRow(
            selectedTabIndex = selectedFileIndex,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 0.dp
        ) {
            flutterFiles.forEachIndexed { index, file ->
                Tab(
                    selected = selectedFileIndex == index,
                    onClick = { selectedFileIndex = index },
                    text = { Text(file.title, fontSize = 12.sp) },
                    icon = {
                        Icon(
                            imageVector = if (file.title.endsWith(".yaml")) Icons.Default.Terminal else Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        // Active File Metadata
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = activeFile.filePath,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${activeFile.content.lines().size} lines",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Syntax Box with Dark Code Viewer
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("code_viewer_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = Color(0xFF0F172A)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = activeFile.content,
                    color = Color(0xFFE2E8F0),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Terminal Commands Setup Guide Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Terminal Run Instructions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val commands = listOf(
                    "flutter create week3_weather_app",
                    "cd week3_weather_app",
                    "flutter pub add http shared_preferences",
                    "flutter run"
                )

                commands.forEach { cmd ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = "$ $cmd",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun getFlutterCodeFiles(): List<FlutterCodeFile> {
    val d = "$"
    return listOf(
        FlutterCodeFile(
            title = "pubspec.yaml",
            filePath = "pubspec.yaml",
            language = "yaml",
            content = """
name: week3_weather_app
description: "Week 3 Flutter Weather App with REST API, Local Storage, and Material 3"
publish_to: "none"
version: 1.0.0+1

environment:
  sdk: ">=3.0.0 <4.0.0"

dependencies:
  flutter:
    sdk: flutter
  http: ^1.2.0
  shared_preferences: ^2.2.2
  cupertino_icons: ^1.0.6

dev_dependencies:
  flutter_test:
    sdk: flutter
  flutter_lints: ^3.0.0

flutter:
  uses-material-design: true
""".trimIndent()
        ),
        FlutterCodeFile(
            title = "weather_model.dart",
            filePath = "lib/models/weather_model.dart",
            language = "dart",
            content = """
class WeatherModel {
  final String cityName;
  final String country;
  final double temperature;
  final double feelsLike;
  final int humidity;
  final String condition;
  final String description;
  final String iconCode;
  final double windSpeed;

  WeatherModel({
    required this.cityName,
    required this.country,
    required this.temperature,
    required this.feelsLike,
    required this.humidity,
    required this.condition,
    required this.description,
    required this.iconCode,
    required this.windSpeed,
  });

  // Factory to parse OpenWeatherMap 2.5 API JSON response
  factory WeatherModel.fromOpenWeatherMapJson(Map<String, dynamic> json) {
    final weatherList = json['weather'] as List<dynamic>?;
    final weatherItem = weatherList != null && weatherList.isNotEmpty ? weatherList[0] : null;
    final main = json['main'] as Map<String, dynamic>? ?? {};
    final wind = json['wind'] as Map<String, dynamic>? ?? {};
    final sys = json['sys'] as Map<String, dynamic>? ?? {};

    return WeatherModel(
      cityName: json['name'] as String? ?? 'Unknown',
      country: sys['country'] as String? ?? '',
      temperature: (main['temp'] as num?)?.toDouble() ?? 0.0,
      feelsLike: (main['feels_like'] as num?)?.toDouble() ?? 0.0,
      humidity: (main['humidity'] as num?)?.toInt() ?? 0,
      condition: weatherItem?['main'] as String? ?? 'Clear',
      description: weatherItem?['description'] as String? ?? 'Clear sky',
      iconCode: weatherItem?['icon'] as String? ?? '01d',
      windSpeed: ((wind['speed'] as num?)?.toDouble() ?? 0.0) * 3.6,
    );
  }

  // Fallback parser for Open-Meteo REST API
  factory WeatherModel.fromOpenMeteoJson({
    required String cityName,
    required String country,
    required Map<String, dynamic> current,
  }) {
    final temp = (current['temperature_2m'] as num?)?.toDouble() ?? 0.0;
    final feels = (current['apparent_temperature'] as num?)?.toDouble() ?? temp;
    final humidity = (current['relative_humidity_2m'] as num?)?.toInt() ?? 0;
    final wind = (current['wind_speed_10m'] as num?)?.toDouble() ?? 0.0;
    final code = (current['weather_code'] as num?)?.toInt() ?? 0;

    return WeatherModel(
      cityName: cityName,
      country: country,
      temperature: temp,
      feelsLike: feels,
      humidity: humidity,
      condition: _parseWeatherCondition(code),
      description: _parseWeatherDesc(code),
      iconCode: '02d',
      windSpeed: wind,
    );
  }

  static String _parseWeatherCondition(int code) {
    if (code == 0) return 'Clear';
    if (code <= 3) return 'Partly Cloudy';
    if (code <= 48) return 'Foggy';
    if (code <= 67) return 'Rain';
    if (code <= 77) return 'Snow';
    if (code >= 95) return 'Thunderstorm';
    return 'Cloudy';
  }

  static String _parseWeatherDesc(int code) {
    if (code == 0) return 'Sunny & Clear';
    if (code <= 3) return 'Partly Cloudy';
    if (code <= 48) return 'Fog & Mist';
    if (code <= 67) return 'Passing Showers';
    if (code <= 77) return 'Light Snow';
    if (code >= 95) return 'Thunderstorm Activity';
    return 'Overcast';
  }
}
""".trimIndent()
        ),
        FlutterCodeFile(
            title = "weather_service.dart",
            filePath = "lib/services/weather_service.dart",
            language = "dart",
            content = """
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/weather_model.dart';

class WeatherService {
  static const String _owmBaseUrl = 'https://api.openweathermap.org/data/2.5/weather';
  static const String _geocodingBaseUrl = 'https://geocoding-api.open-meteo.com/v1/search';
  static const String _meteoBaseUrl = 'https://api.open-meteo.com/v1/forecast';

  final String? openWeatherMapApiKey;

  WeatherService({this.openWeatherMapApiKey});

  Future<WeatherModel> fetchWeather(String cityName) async {
    final query = cityName.trim();
    if (query.isEmpty) {
      throw Exception('City name cannot be empty.');
    }

    if (openWeatherMapApiKey != null && openWeatherMapApiKey!.isNotEmpty) {
      final uri = Uri.parse(
        '§_owmBaseUrl?q=§{Uri.encodeComponent(query)}&appid=§openWeatherMapApiKey&units=metric',
      );

      final response = await http.get(uri);
      if (response.statusCode == 200) {
        final Map<String, dynamic> data = json.decode(response.body);
        return WeatherModel.fromOpenWeatherMapJson(data);
      } else if (response.statusCode == 404) {
        throw Exception('City "§query" not found. Please verify spelling.');
      } else if (response.statusCode == 401) {
        throw Exception('Invalid OpenWeatherMap API key.');
      } else {
        throw Exception('Failed to load weather: HTTP §{response.statusCode}');
      }
    }

    // Default Zero-Config Fallback: Open-Meteo REST API
    final geoUri = Uri.parse('§_geocodingBaseUrl?name=§{Uri.encodeComponent(query)}&count=1&language=en&format=json');
    final geoResponse = await http.get(geoUri);

    if (geoResponse.statusCode != 200) {
      throw Exception('Network error while searching for city.');
    }

    final Map<String, dynamic> geoData = json.decode(geoResponse.body);
    final results = geoData['results'] as List<dynamic>?;
    if (results == null || results.isEmpty) {
      throw Exception('City "§query" not found. Please check spelling.');
    }

    final first = results[0] as Map<String, dynamic>;
    final lat = first['latitude'];
    final lon = first['longitude'];
    final resolvedCity = first['name'] as String? ?? query;
    final country = first['country'] as String? ?? '';

    final weatherUri = Uri.parse(
      '§_meteoBaseUrl?latitude=§lat&longitude=§lon&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m&timezone=auto',
    );

    final weatherResponse = await http.get(weatherUri);
    if (weatherResponse.statusCode != 200) {
      throw Exception('Failed to fetch weather data for §resolvedCity.');
    }

    final Map<String, dynamic> weatherData = json.decode(weatherResponse.body);
    final current = weatherData['current'] as Map<String, dynamic>?;
    if (current == null) {
      throw Exception('No weather metrics returned for §resolvedCity.');
    }

    return WeatherModel.fromOpenMeteoJson(
      cityName: resolvedCity,
      country: country,
      current: current,
    );
  }
}
""".trimIndent().replace("§", d)
        ),
        FlutterCodeFile(
            title = "storage_service.dart",
            filePath = "lib/services/storage_service.dart",
            language = "dart",
            content = """
import 'package:shared_preferences/shared_preferences.dart';

class StorageService {
  static const String keyRememberMe = 'remember_me';
  static const String keyLastCity = 'last_searched_city';

  static Future<void> saveRememberMe(bool value) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(keyRememberMe, value);
  }

  static Future<bool> getRememberMe() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(keyRememberMe) ?? false;
  }

  static Future<void> saveLastCity(String city) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(keyLastCity, city.trim());
  }

  static Future<String> getLastCity({String defaultCity = 'London'}) async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(keyLastCity) ?? defaultCity;
  }

  static Future<void> clearAll() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
  }
}
""".trimIndent()
        ),
        FlutterCodeFile(
            title = "weather_screen.dart",
            filePath = "lib/screens/weather_screen.dart",
            language = "dart",
            content = """
import 'package:flutter/material.dart';
import '../models/weather_model.dart';
import '../services/weather_service.dart';
import '../services/storage_service.dart';
import '../widgets/weather_info_card.dart';

class WeatherScreen extends StatefulWidget {
  const WeatherScreen({super.key});

  @override
  State<WeatherScreen> createState() => _WeatherScreenState();
}

class _WeatherScreenState extends State<WeatherScreen> {
  final TextEditingController _cityController = TextEditingController();
  final WeatherService _weatherService = WeatherService();

  WeatherModel? _weather;
  bool _isLoading = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadInitialCity();
  }

  Future<void> _loadInitialCity() async {
    final lastCity = await StorageService.getLastCity(defaultCity: 'London');
    _cityController.text = lastCity;
    _fetchWeather(lastCity);
  }

  Future<void> _fetchWeather(String cityName) async {
    if (cityName.trim().isEmpty) {
      setState(() => _errorMessage = 'Please enter a city name.');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final result = await _weatherService.fetchWeather(cityName);
      setState(() {
        _weather = result;
        _isLoading = false;
        _errorMessage = null;
      });
      await StorageService.saveLastCity(result.cityName);
    } catch (e) {
      setState(() {
        _isLoading = false;
        _weather = null;
        _errorMessage = e.toString().replaceFirst('Exception: ', '');
      });
    }
  }

  @override
  void dispose() {
    _cityController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Weather Mini-Project', style: TextStyle(fontWeight: FontWeight.bold)),
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              controller: _cityController,
              decoration: InputDecoration(
                hintText: 'Enter city name (e.g., Tokyo, London)...',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: IconButton(
                  icon: const Icon(Icons.arrow_forward),
                  onPressed: () => _fetchWeather(_cityController.text),
                ),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(16)),
                filled: true,
              ),
              onSubmitted: _fetchWeather,
            ),
            const SizedBox(height: 24),
            if (_isLoading)
              const Center(
                child: Padding(
                  padding: EdgeInsets.symmetric(vertical: 48),
                  child: CircularProgressIndicator(),
                ),
              )
            else if (_errorMessage != null)
              Card(
                color: Theme.of(context).colorScheme.errorContainer,
                shape: RoundedCornerShape(16),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    children: [
                      Icon(Icons.error_outline, color: Theme.of(context).colorScheme.error, size: 40),
                      const SizedBox(height: 8),
                      Text(
                        _errorMessage!,
                        style: TextStyle(color: Theme.of(context).colorScheme.onErrorContainer),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 12),
                      ElevatedButton(
                        onPressed: () => _fetchWeather(_cityController.text),
                        child: const Text('Try Again'),
                      ),
                    ],
                  ),
                ),
              )
            else if (_weather != null)
              WeatherInfoCard(weather: _weather!),
          ],
        ),
      ),
    );
  }
}
""".trimIndent()
        ),
        FlutterCodeFile(
            title = "weather_info_card.dart",
            filePath = "lib/widgets/weather_info_card.dart",
            language = "dart",
            content = """
import 'package:flutter/material.dart';
import '../models/weather_model.dart';

class WeatherInfoCard extends StatelessWidget {
  final WeatherModel weather;

  const WeatherInfoCard({super.key, required this.weather});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Card(
      elevation: 4,
      shape: RoundedCornerShape(24),
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          children: [
            Text(
              '§{weather.cityName}§{weather.country.isNotEmpty ? ", " + weather.country : ""}',
              style: theme.textTheme.headlineMedium?.copyWith(fontWeight: FontWeight.bold),
            ),
            Text(
              weather.description,
              style: theme.textTheme.titleMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant),
            ),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Image.network(
                  'https://openweathermap.org/img/wn/§{weather.iconCode}@2x.png',
                  width: 80,
                  height: 80,
                  errorBuilder: (_, __, ___) => const Icon(Icons.wb_sunny, size: 64, color: Colors.amber),
                ),
                const SizedBox(width: 16),
                Text(
                  '§{weather.temperature.round()}°C',
                  style: theme.textTheme.displayLarge?.copyWith(fontWeight: FontWeight.bold),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _MetricItem(
                  icon: Icons.water_drop,
                  label: 'Humidity',
                  value: '§{weather.humidity}%',
                ),
                _MetricItem(
                  icon: Icons.air,
                  label: 'Wind Speed',
                  value: '§{weather.windSpeed.round()} km/h',
                ),
                _MetricItem(
                  icon: Icons.thermostat,
                  label: 'Feels Like',
                  value: '§{weather.feelsLike.round()}°C',
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _MetricItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;

  const _MetricItem({required this.icon, required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Icon(icon, color: Theme.of(context).colorScheme.primary),
        const SizedBox(height: 4),
        Text(label, style: const TextStyle(fontSize: 12, color: Colors.grey)),
        Text(value, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.bold)),
      ],
    );
  }
}
""".trimIndent().replace("§", d)
        ),
        FlutterCodeFile(
            title = "main.dart",
            filePath = "lib/main.dart",
            language = "dart",
            content = """
import 'package:flutter/material.dart';
import 'screens/weather_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const Week3WeatherApp());
}

class Week3WeatherApp extends StatelessWidget {
  const Week3WeatherApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Week 3 Weather App',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF0284C7),
          brightness: Brightness.light,
        ),
      ),
      darkTheme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF0284C7),
          brightness: Brightness.dark,
        ),
      ),
      themeMode: ThemeMode.system,
      home: const WeatherScreen(),
    );
  }
}
""".trimIndent()
        )
    )
}
