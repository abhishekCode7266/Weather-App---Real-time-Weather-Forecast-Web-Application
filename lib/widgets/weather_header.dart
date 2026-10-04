import 'package:flutter/material.dart';

class WeatherHeader extends StatefulWidget {
  final Function(String) onSearch;
  final VoidCallback onRefresh;
  final VoidCallback onToggleUnit;
  final VoidCallback onOpenSettings;
  final bool isCelsius;

  const WeatherHeader({
    super.key,
    required this.onSearch,
    required this.onRefresh,
    required this.onToggleUnit,
    required this.onOpenSettings,
    required this.isCelsius,
  });

  @override
  State<WeatherHeader> createState() => _WeatherHeaderState();
}

class _WeatherHeaderState extends State<WeatherHeader> {
  final TextEditingController _controller = TextEditingController();

  final List<String> _quickCities = [
    'New Delhi',
    'Mumbai',
    'London',
    'New York',
    'Tokyo',
    'Paris',
    'Dubai',
  ];

  void _submit() {
    final query = _controller.text.trim();
    if (query.isNotEmpty) {
      widget.onSearch(query);
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Top App Bar row
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [Color(0xFF0284C7), Color(0xFF38BDF8)],
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                    ),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Icon(Icons.cloud, color: Colors.white, size: 24),
                ),
                const SizedBox(width: 12),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Live Weather',
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                        letterSpacing: -0.3,
                      ),
                    ),
                    Text(
                      'Real-time Global Forecast',
                      style: theme.textTheme.labelSmall?.copyWith(
                        color: colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ],
            ),
            Row(
              children: [
                // Unit Switcher (°C / °F)
                ActionChip(
                  avatar: Icon(
                    widget.isCelsius ? Icons.thermostat : Icons.device_thermostat,
                    size: 16,
                    color: colorScheme.primary,
                  ),
                  label: Text(
                    widget.isCelsius ? '°C' : '°F',
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                  ),
                  onPressed: widget.onToggleUnit,
                  visualDensity: VisualDensity.compact,
                ),
                const SizedBox(width: 6),
                // Refresh Button
                IconButton.filledTonal(
                  icon: const Icon(Icons.refresh, size: 20),
                  onPressed: widget.onRefresh,
                  tooltip: 'Refresh Weather',
                ),
                const SizedBox(width: 4),
                // Settings Button
                IconButton.outlined(
                  icon: const Icon(Icons.settings_outlined, size: 20),
                  onPressed: widget.onOpenSettings,
                  tooltip: 'Settings & API Key',
                ),
              ],
            ),
          ],
        ),

        const SizedBox(height: 16),

        // Search Bar
        Material(
          elevation: 2,
          shadowColor: Colors.black12,
          borderRadius: BorderRadius.circular(16),
          child: TextField(
            controller: _controller,
            textInputAction: TextInputAction.search,
            onSubmitted: (_) => _submit(),
            decoration: InputDecoration(
              hintText: 'Search city name (e.g., Delhi, London, Tokyo)...',
              prefixIcon: const Icon(Icons.search),
              suffixIcon: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  if (_controller.text.isNotEmpty)
                    IconButton(
                      icon: const Icon(Icons.clear, size: 18),
                      onPressed: () {
                        setState(() {
                          _controller.clear();
                        });
                      },
                    ),
                  IconButton(
                    icon: const Icon(Icons.arrow_forward_rounded),
                    color: colorScheme.primary,
                    onPressed: _submit,
                  ),
                ],
              ),
              filled: true,
              fillColor: colorScheme.surfaceContainerHighest.withOpacity(0.5),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(16),
                borderSide: BorderSide.none,
              ),
              contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
            ),
            onChanged: (text) => setState(() {}),
          ),
        ),

        const SizedBox(height: 10),

        // Quick City Chips
        SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: Row(
            children: _quickCities.map((city) {
              return Padding(
                padding: const EdgeInsets.only(right: 6.0),
                child: ActionChip(
                  label: Text(city, style: const TextStyle(fontSize: 12)),
                  backgroundColor: colorScheme.surfaceContainerLow,
                  onPressed: () {
                    _controller.text = city;
                    widget.onSearch(city);
                  },
                ),
              );
            }).toList(),
          ),
        ),
      ],
    );
  }
}
