package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseSimulatorRepository
import com.example.network.NewsRepository
import com.example.network.WeatherRepository
import com.example.storage.AppPreferences
import com.example.ui.feed.NewsFeedScreen
import com.example.ui.firebase.FirebaseAuthCrudScreen
import com.example.ui.fluttercode.FlutterCodeHubScreen
import com.example.ui.storage.StorageScreen
import com.example.ui.weather.WeatherScreen

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    WEATHER("Weather", Icons.Filled.WbSunny, Icons.Outlined.WbSunny, "tab_weather"),
    FEED("Feed", Icons.Filled.Article, Icons.Outlined.Article, "tab_feed"),
    STORAGE("Storage", Icons.Filled.Storage, Icons.Outlined.Storage, "tab_storage"),
    FIREBASE("Firebase", Icons.Filled.CloudSync, Icons.Outlined.CloudSync, "tab_firebase"),
    CODE("Flutter", Icons.Filled.Code, Icons.Outlined.Code, "tab_code")
}

@Composable
fun MainApp(
    weatherRepo: WeatherRepository,
    newsRepo: NewsRepository,
    appPreferences: AppPreferences,
    firebaseRepo: FirebaseSimulatorRepository
) {
    var currentTab by remember { mutableStateOf(AppTab.WEATHER) }

    // Handle back button if on sub-screens
    BackHandler(enabled = currentTab != AppTab.WEATHER) {
        currentTab = AppTab.WEATHER
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("main_bottom_nav_bar")) {
                AppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title, fontSize = 11.sp) },
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            AppTab.WEATHER -> WeatherScreen(
                weatherRepo = weatherRepo,
                appPreferences = appPreferences,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.FEED -> NewsFeedScreen(
                newsRepo = newsRepo,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.STORAGE -> StorageScreen(
                appPreferences = appPreferences,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.FIREBASE -> FirebaseAuthCrudScreen(
                firebaseRepo = firebaseRepo,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.CODE -> FlutterCodeHubScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
