import 'package:shared_preferences/shared_preferences.dart';

/// Service for persisting user preferences and recent searches locally.
class StorageService {
  static const String _keyLastCity = 'last_searched_city';
  static const String _keyRecentCities = 'recent_searched_cities';
  static const String _keyIsCelsius = 'unit_is_celsius';
  static const String _keyApiKey = 'custom_openweather_api_key';

  /// Save last searched city
  static Future<void> saveLastCity(String city) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyLastCity, city.trim());
    await addRecentSearch(city.trim());
  }

  /// Get last searched city
  static Future<String> getLastCity({String defaultCity = 'New Delhi'}) async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyLastCity) ?? defaultCity;
  }

  /// Add city to recent search history
  static Future<void> addRecentSearch(String city) async {
    final prefs = await SharedPreferences.getInstance();
    final list = prefs.getStringList(_keyRecentCities) ?? [];
    list.remove(city);
    list.insert(0, city);
    if (list.length > 8) {
      list.removeRange(8, list.length);
    }
    await prefs.setStringList(_keyRecentCities, list);
  }

  /// Get recent searched cities list
  static Future<List<String>> getRecentSearches() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getStringList(_keyRecentCities) ?? ['New Delhi', 'London', 'New York', 'Tokyo'];
  }

  /// Unit preference: Celsius vs Fahrenheit
  static Future<bool> getIsCelsius() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_keyIsCelsius) ?? true;
  }

  static Future<void> setIsCelsius(bool value) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyIsCelsius, value);
  }

  /// Custom OpenWeatherMap API Key
  static Future<String?> getApiKey() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyApiKey);
  }

  static Future<void> setApiKey(String key) async {
    final prefs = await SharedPreferences.getInstance();
    if (key.trim().isEmpty) {
      await prefs.remove(_keyApiKey);
    } else {
      await prefs.setString(_keyApiKey, key.trim());
    }
  }

  /// Clear all stored data
  static Future<void> clearAll() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
  }
}
