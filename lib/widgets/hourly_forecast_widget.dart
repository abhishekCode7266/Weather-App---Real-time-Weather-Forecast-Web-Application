import 'package:flutter/material.dart';
import '../models/weather_model.dart';

class HourlyForecastWidget extends StatelessWidget {
  final List<HourlyForecast> hourly;
  final bool isCelsius;

  const HourlyForecastWidget({
    super.key,
    required this.hourly,
    required this.isCelsius,
  });

  String _formatTemp(double temp) {
    if (isCelsius) {
      return '${temp.round()}°';
    }
    final f = (temp * 9 / 5) + 32;
    return '${f.round()}°';
  }

  @override
  Widget build(BuildContext context) {
    if (hourly.isEmpty) return const SizedBox.shrink();

    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(Icons.access_time_rounded, size: 18, color: colorScheme.primary),
            const SizedBox(width: 8),
            Text(
              'Hourly Forecast (Next 24h)',
              style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold),
            ),
          ],
        ),
        const SizedBox(height: 12),
        SizedBox(
          height: 120,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: hourly.length,
            separatorBuilder: (_, __) => const SizedBox(width: 8),
            itemBuilder: (context, index) {
              final item = hourly[index];
              return Container(
                width: 76,
                padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 6),
                decoration: BoxDecoration(
                  color: colorScheme.surfaceContainerHighest.withOpacity(0.4),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(
                    color: colorScheme.outlineVariant.withOpacity(0.3),
                  ),
                ),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      item.time,
                      style: theme.textTheme.labelSmall?.copyWith(
                        fontWeight: FontWeight.w600,
                        color: colorScheme.onSurfaceVariant,
                      ),
                    ),
                    Image.network(
                      'https://openweathermap.org/img/wn/${item.iconCode}.png',
                      width: 38,
                      height: 38,
                      errorBuilder: (_, __, ___) => Icon(
                        Icons.cloud_outlined,
                        size: 26,
                        color: colorScheme.primary,
                      ),
                    ),
                    Text(
                      _formatTemp(item.temperature),
                      style: theme.textTheme.labelLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}
