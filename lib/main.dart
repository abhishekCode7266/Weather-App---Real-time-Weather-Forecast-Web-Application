import 'package:flutter/material.dart';
import 'screens/weather_home_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const WeatherApp());
}

class WeatherApp extends StatelessWidget {
  const WeatherApp({super.key});

  @override
  Widget build(BuildContext context) {
    const seedColor = Color(0xFF0284C7);

    return MaterialApp(
      title: 'Real-time Weather Forecast',
      debugShowCheckedModeBanner: false,
      themeMode: ThemeMode.system,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: seedColor,
          brightness: Brightness.light,
        ),
        scaffoldBackgroundColor: const Color(0xFFF8FAFC),
        cardTheme: CardTheme(
          elevation: 0,
          shape: RoundedCornerShape(20),
        ),
      ),
      darkTheme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: seedColor,
          brightness: Brightness.dark,
        ),
        scaffoldBackgroundColor: const Color(0xFF0B132B),
        cardTheme: CardTheme(
          elevation: 0,
          shape: RoundedCornerShape(20),
        ),
      ),
      home: const WeatherHomeScreen(),
    );
  }
}
