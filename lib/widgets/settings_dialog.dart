import 'package:flutter/material.dart';
import '../services/storage_service.dart';

class SettingsDialog extends StatefulWidget {
  final bool isCelsius;
  final Function(bool) onUnitChanged;
  final VoidCallback onDataChanged;

  const SettingsDialog({
    super.key,
    required this.isCelsius,
    required this.onUnitChanged,
    required this.onDataChanged,
  });

  @override
  State<SettingsDialog> createState() => _SettingsDialogState();
}

class _SettingsDialogState extends State<SettingsDialog> {
  final TextEditingController _apiKeyController = TextEditingController();
  bool _isCelsius = true;

  @override
  void initState() {
    super.initState();
    _isCelsius = widget.isCelsius;
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    final key = await StorageService.getApiKey();
    if (key != null) {
      _apiKeyController.text = key;
    }
  }

  Future<void> _saveSettings() async {
    await StorageService.setIsCelsius(_isCelsius);
    await StorageService.setApiKey(_apiKeyController.text);
    widget.onUnitChanged(_isCelsius);
    widget.onDataChanged();
    if (mounted) {
      Navigator.of(context).pop();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Settings saved successfully!'),
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  @override
  void dispose() {
    _apiKeyController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return AlertDialog(
      title: Row(
        children: [
          Icon(Icons.tune_rounded, color: colorScheme.primary),
          const SizedBox(width: 8),
          const Text('Preferences & API'),
        ],
      ),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Temperature Unit Switch
            Text(
              'Temperature Unit',
              style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            SegmentedButton<bool>(
              segments: const [
                ButtonSegment<bool>(
                  value: true,
                  label: Text('Celsius (°C)'),
                  icon: Icon(Icons.thermostat),
                ),
                ButtonSegment<bool>(
                  value: false,
                  label: Text('Fahrenheit (°F)'),
                  icon: Icon(Icons.device_thermostat),
                ),
              ],
              selected: {_isCelsius},
              onSelectionChanged: (Set<bool> newSelection) {
                setState(() {
                  _isCelsius = newSelection.first;
                });
              },
            ),

            const SizedBox(height: 20),

            // OpenWeatherMap API Key
            Text(
              'OpenWeatherMap API Key (Optional)',
              style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 6),
            Text(
              'By default, the app uses the free Open-Meteo REST API (zero setup needed). If you have an OpenWeatherMap API key, you can enter it here.',
              style: theme.textTheme.bodySmall?.copyWith(color: colorScheme.onSurfaceVariant),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _apiKeyController,
              decoration: InputDecoration(
                hintText: 'Enter API Key (optional)...',
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                isDense: true,
                suffixIcon: _apiKeyController.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear, size: 16),
                        onPressed: () => setState(() => _apiKeyController.clear()),
                      )
                    : null,
              ),
              onChanged: (_) => setState(() {}),
            ),

            const SizedBox(height: 18),

            // About Section
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: colorScheme.surfaceContainerHighest.withOpacity(0.5),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Real-time Weather Forecast v1.0.0',
                    style: theme.textTheme.labelMedium?.copyWith(fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Live global weather data, 24-hour hourly outlook, and 7-day extended forecasts powered by Open-Meteo & OpenWeatherMap APIs.',
                    style: theme.textTheme.bodySmall?.copyWith(fontSize: 11),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        FilledButton(
          onPressed: _saveSettings,
          child: const Text('Save Changes'),
        ),
      ],
    );
  }
}
