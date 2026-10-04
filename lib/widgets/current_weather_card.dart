import 'package:flutter/material.dart';
import '../models/weather_model.dart';

class CurrentWeatherCard extends StatelessWidget {
  final WeatherModel weather;
  final bool isCelsius;

  const CurrentWeatherCard({
    super.key,
    required this.weather,
    required this.isCelsius,
  });

  List<Color> _getGradientColors() {
    final condition = weather.condition.toLowerCase();
    final isDay = weather.isDay;

    if (condition.contains('thunder')) {
      return const [Color(0xFF2E1065), Color(0xFF1E1B4B)];
    } else if (condition.contains('rain') || condition.contains('drizzle')) {
      return const [Color(0xFF1E293B), Color(0xFF334155)];
    } else if (condition.contains('snow')) {
      return const [Color(0xFF334155), Color(0xFF475569)];
    } else if (condition.contains('clear')) {
      if (isDay) {
        return const [Color(0xFF0284C7), Color(0xFF38BDF8)];
      } else {
        return const [Color(0xFF0F172A), Color(0xFF1E293B)];
      }
    } else if (condition.contains('cloud')) {
      if (isDay) {
        return const [Color(0xFF0369A1), Color(0xFF64748B)];
      } else {
        return const [Color(0xFF1E293B), Color(0xFF334155)];
      }
    }

    return const [Color(0xFF0284C7), Color(0xFF0EA5E9)];
  }

  IconData _getFallbackIcon() {
    final condition = weather.condition.toLowerCase();
    if (condition.contains('clear')) {
      return weather.isDay ? Icons.wb_sunny_rounded : Icons.nightlight_round;
    } else if (condition.contains('rain')) {
      return Icons.grain_rounded;
    } else if (condition.contains('snow')) {
      return Icons.ac_unit_rounded;
    } else if (condition.contains('thunder')) {
      return Icons.flash_on_rounded;
    }
    return Icons.cloud_rounded;
  }

  @override
  Widget build(BuildContext context) {
    final gradient = _getGradientColors();

    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: gradient,
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(28),
        boxShadow: [
          BoxShadow(
            color: gradient[0].withOpacity(0.35),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      padding: const EdgeInsets.all(24.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // City Name, Country & Time
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.location_on, color: Colors.white70, size: 18),
                        const SizedBox(width: 4),
                        Flexible(
                          child: Text(
                            weather.cityName,
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 26,
                              fontWeight: FontWeight.bold,
                              letterSpacing: -0.5,
                            ),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ],
                    ),
                    if (weather.country.isNotEmpty)
                      Padding(
                        padding: const EdgeInsets.only(left: 22.0, top: 2),
                        child: Text(
                          weather.country,
                          style: TextStyle(
                            color: Colors.white.withOpacity(0.8),
                            fontSize: 14,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ),
                  ],
                ),
              ),
              // Day/Night Indicator Badge
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.18),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      weather.isDay ? Icons.wb_sunny : Icons.bedtime,
                      size: 14,
                      color: Colors.white,
                    ),
                    const SizedBox(width: 5),
                    Text(
                      weather.isDay ? 'Day' : 'Night',
                      style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.w600),
                    ),
                  ],
                ),
              ),
            ],
          ),

          const SizedBox(height: 20),

          // Main Temperature and Weather Icon
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    weather.formattedTemp(isCelsius: isCelsius),
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 64,
                      fontWeight: FontWeight.w800,
                      height: 1.0,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    weather.description,
                    style: TextStyle(
                      color: Colors.white.withOpacity(0.95),
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Feels like ${weather.formattedFeelsLike(isCelsius: isCelsius)}',
                    style: TextStyle(
                      color: Colors.white.withOpacity(0.8),
                      fontSize: 14,
                    ),
                  ),
                ],
              ),
              // Weather Condition Icon
              Container(
                width: 96,
                height: 96,
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.15),
                  shape: BoxShape.circle,
                ),
                child: Image.network(
                  weather.iconUrl,
                  fit: BoxFit.contain,
                  errorBuilder: (_, __, ___) => Icon(
                    _getFallbackIcon(),
                    size: 54,
                    color: Colors.white,
                  ),
                ),
              ),
            ],
          ),

          const SizedBox(height: 24),
          const Divider(color: Colors.white24, height: 1),
          const SizedBox(height: 18),

          // Weather Metrics Grid (Humidity, Wind, Pressure, Min/Max)
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _MetricTile(
                icon: Icons.water_drop_outlined,
                label: 'Humidity',
                value: '${weather.humidity}%',
              ),
              _MetricTile(
                icon: Icons.air,
                label: 'Wind',
                value: '${weather.windSpeed.round()} km/h',
              ),
              _MetricTile(
                icon: Icons.speed,
                label: 'Pressure',
                value: '${weather.pressure} hPa',
              ),
              _MetricTile(
                icon: Icons.thermostat_outlined,
                label: 'Range',
                value: isCelsius
                    ? '${weather.tempMin.round()}° / ${weather.tempMax.round()}°'
                    : '${((weather.tempMin * 9 / 5) + 32).round()}° / ${((weather.tempMax * 9 / 5) + 32).round()}°',
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _MetricTile extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;

  const _MetricTile({
    required this.icon,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.15),
            shape: BoxShape.circle,
          ),
          child: Icon(icon, color: Colors.white, size: 20),
        ),
        const SizedBox(height: 6),
        Text(
          label,
          style: TextStyle(
            color: Colors.white.withOpacity(0.75),
            fontSize: 11,
            fontWeight: FontWeight.w500,
          ),
        ),
        const SizedBox(height: 2),
        Text(
          value,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 13,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }
}
