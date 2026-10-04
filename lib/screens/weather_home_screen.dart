import 'package:flutter/material.dart';
import '../models/weather_model.dart';
import '../services/weather_service.dart';
import '../services/storage_service.dart';
import '../widgets/weather_header.dart';
import '../widgets/current_weather_card.dart';
import '../widgets/hourly_forecast_widget.dart';
import '../widgets/daily_forecast_widget.dart';
import '../widgets/settings_dialog.dart';

class WeatherHomeScreen extends StatefulWidget {
  const WeatherHomeScreen({super.key});

  @override
  State<WeatherHomeScreen> createState() => _WeatherHomeScreenState();
}

class _WeatherHomeScreenState extends State<WeatherHomeScreen> {
  final WeatherService _weatherService = WeatherService();

  WeatherModel? _weather;
  bool _isLoading = true;
  String? _errorMessage;
  String _currentCity = 'New Delhi';
  bool _isCelsius = true;

  @override
  void initState() {
    super.initState();
    _initializeApp();
  }

  Future<void> _initializeApp() async {
    final isCelsius = await StorageService.getIsCelsius();
    final lastCity = await StorageService.getLastCity(defaultCity: 'New Delhi');
    setState(() {
      _isCelsius = isCelsius;
      _currentCity = lastCity;
    });
    await _loadWeather(_currentCity);
  }

  Future<void> _loadWeather(String cityName) async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final customKey = await StorageService.getApiKey();
      final result = await _weatherService.fetchWeather(cityName, customApiKey: customKey);

      if (mounted) {
        setState(() {
          _weather = result;
          _currentCity = result.cityName;
          _isLoading = false;
          _errorMessage = null;
        });
        await StorageService.saveLastCity(result.cityName);
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoading = false;
          _errorMessage = e.toString().replaceFirst('Exception: ', '');
        });
      }
    }
  }

  void _toggleUnit() async {
    final newUnit = !_isCelsius;
    setState(() {
      _isCelsius = newUnit;
    });
    await StorageService.setIsCelsius(newUnit);
  }

  void _openSettings() {
    showDialog(
      context: context,
      builder: (ctx) => SettingsDialog(
        isCelsius: _isCelsius,
        onUnitChanged: (val) {
          setState(() {
            _isCelsius = val;
          });
        },
        onDataChanged: () {
          _loadWeather(_currentCity);
        },
      ),
    );
  }

  @override
  void dispose() {
    _weatherService.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Scaffold(
      body: SafeArea(
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 720),
            child: RefreshIndicator(
              onRefresh: () => _loadWeather(_currentCity),
              color: colorScheme.primary,
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    // Top App Header & Search Bar
                    WeatherHeader(
                      onSearch: (city) => _loadWeather(city),
                      onRefresh: () => _loadWeather(_currentCity),
                      onToggleUnit: _toggleUnit,
                      onOpenSettings: _openSettings,
                      isCelsius: _isCelsius,
                    ),

                    const SizedBox(height: 16),

                    // Loading State
                    if (_isLoading)
                      Padding(
                        padding: const EdgeInsets.symmetric(vertical: 60),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            CircularProgressIndicator(
                              strokeWidth: 3,
                              color: colorScheme.primary,
                            ),
                            const SizedBox(height: 16),
                            Text(
                              'Fetching live weather for "$_currentCity"...',
                              style: theme.textTheme.bodyMedium?.copyWith(
                                color: colorScheme.onSurfaceVariant,
                              ),
                            ),
                          ],
                        ),
                      )

                    // Error State
                    else if (_errorMessage != null)
                      Card(
                        elevation: 0,
                        color: colorScheme.errorContainer.withOpacity(0.7),
                        shape: RoundedCornerShape(20),
                        child: Padding(
                          padding: const EdgeInsets.all(20.0),
                          child: Column(
                            children: [
                              Icon(
                                Icons.cloud_off_rounded,
                                size: 48,
                                color: colorScheme.error,
                              ),
                              const SizedBox(height: 12),
                              Text(
                                'Unable to Load Weather',
                                style: theme.textTheme.titleMedium?.copyWith(
                                  fontWeight: FontWeight.bold,
                                  color: colorScheme.onErrorContainer,
                                ),
                              ),
                              const SizedBox(height: 6),
                              Text(
                                _errorMessage!,
                                textAlign: TextAlign.center,
                                style: theme.textTheme.bodyMedium?.copyWith(
                                  color: colorScheme.onErrorContainer,
                                ),
                              ),
                              const SizedBox(height: 16),
                              FilledButton.icon(
                                icon: const Icon(Icons.refresh, size: 18),
                                label: const Text('Try Again'),
                                onPressed: () => _loadWeather(_currentCity),
                              ),
                            ],
                          ),
                        ),
                      )

                    // Weather Loaded Successfully
                    else if (_weather != null) ...[
                      // Main Current Weather Card
                      CurrentWeatherCard(
                        weather: _weather!,
                        isCelsius: _isCelsius,
                      ),

                      const SizedBox(height: 20),

                      // Hourly Forecast
                      if (_weather!.hourlyForecast.isNotEmpty) ...[
                        HourlyForecastWidget(
                          hourly: _weather!.hourlyForecast,
                          isCelsius: _isCelsius,
                        ),
                        const SizedBox(height: 20),
                      ],

                      // 7-Day Forecast
                      if (_weather!.dailyForecast.isNotEmpty) ...[
                        DailyForecastWidget(
                          daily: _weather!.dailyForecast,
                          isCelsius: _isCelsius,
                        ),
                        const SizedBox(height: 20),
                      ],

                      // Footer Info
                      Padding(
                        padding: const EdgeInsets.symmetric(vertical: 12.0),
                        child: Center(
                          child: Text(
                            'Real-time data powered by Open-Meteo & OpenWeatherMap',
                            style: theme.textTheme.labelSmall?.copyWith(
                              color: colorScheme.outline,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
