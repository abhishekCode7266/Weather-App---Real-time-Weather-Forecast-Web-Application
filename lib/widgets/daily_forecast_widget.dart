import 'package:flutter/material.dart';
import '../models/weather_model.dart';

class DailyForecastWidget extends StatelessWidget {
  final List<DailyForecast> daily;
  final bool isCelsius;

  const DailyForecastWidget({
    super.key,
    required this.daily,
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
    if (daily.isEmpty) return const SizedBox.shrink();

    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(Icons.calendar_month_rounded, size: 18, color: colorScheme.primary),
            const SizedBox(width: 8),
            Text(
              '7-Day Weather Forecast',
              style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Container(
          decoration: BoxDecoration(
            color: colorScheme.surfaceContainerHighest.withOpacity(0.3),
            borderRadius: BorderRadius.circular(20),
            border: Border.all(
              color: colorScheme.outlineVariant.withOpacity(0.3),
            ),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          child: Column(
            children: daily.map((day) {
              return Padding(
                padding: const EdgeInsets.symmetric(vertical: 8.0),
                child: Row(
                  children: [
                    // Day of the week
                    SizedBox(
                      width: 70,
                      child: Text(
                        day.dayName,
                        style: TextStyle(
                          fontWeight: day.dayName == 'Today' ? FontWeight.bold : FontWeight.w500,
                          fontSize: 14,
                          color: day.dayName == 'Today' ? colorScheme.primary : colorScheme.onSurface,
                        ),
                      ),
                    ),

                    // Weather Icon & condition
                    Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Image.network(
                          'https://openweathermap.org/img/wn/${day.iconCode}.png',
                          width: 34,
                          height: 34,
                          errorBuilder: (_, __, ___) => Icon(
                            Icons.wb_sunny_outlined,
                            size: 20,
                            color: colorScheme.secondary,
                          ),
                        ),
                        const SizedBox(width: 6),
                        SizedBox(
                          width: 80,
                          child: Text(
                            day.condition,
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: colorScheme.onSurfaceVariant,
                            ),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ],
                    ),

                    const Spacer(),

                    // Min Temp
                    Text(
                      _formatTemp(day.tempMin),
                      style: theme.textTheme.bodyMedium?.copyWith(
                        color: colorScheme.onSurfaceVariant,
                      ),
                    ),

                    // Temperature Bar
                    Container(
                      margin: const EdgeInsets.symmetric(horizontal: 10),
                      width: 50,
                      height: 5,
                      decoration: BoxDecoration(
                        gradient: const LinearGradient(
                          colors: [Color(0xFF38BDF8), Color(0xFFF97316)],
                        ),
                        borderRadius: BorderRadius.circular(3),
                      ),
                    ),

                    // Max Temp
                    SizedBox(
                      width: 32,
                      child: Text(
                        _formatTemp(day.tempMax),
                        textAlign: TextAlign.end,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ],
                ),
              );
            }).toList(),
          ),
        ),
      ],
    );
  }
}
