package com.alibadi.quran.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alibadi.quran.R
import com.alibadi.quran.feature.settings.SettingsViewModel
import com.alibadi.quran.ui.navigation.AppNavigation
import com.alibadi.quran.ui.theme.MyApplicationTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val settingsViewModel: SettingsViewModel = koinViewModel()

    val themeMode by settingsViewModel.themeMode.collectAsState()
    val dynamicColorEnabled by settingsViewModel.dynamicColor.collectAsState()
    val amoled by settingsViewModel.amoled.collectAsState()
    val fontScale by settingsViewModel.fontScale.collectAsState()

    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = remember(themeMode, isSystemDark) {
        when (themeMode) {
            "LIGHT" -> false
            "DARK" -> true
            else -> isSystemDark
        }
    }

    MyApplicationTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColorEnabled,
        amoled = amoled,
        fontScale = fontScale
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

        val showBackButton = currentRoute !in listOf("dashboard", "quran", "adhkar", "calendar", "settings")

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(38.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        shape = MaterialTheme.shapes.small
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    title = {
                        val titleRes = when {
                            currentRoute.startsWith("quran/") -> R.string.screen_quran
                            currentRoute == "quran" -> R.string.screen_quran
                            currentRoute == "dashboard" -> R.string.screen_dashboard
                            currentRoute == "adhkar" -> R.string.screen_adhkar
                            currentRoute == "calendar" -> R.string.screen_calendar
                            currentRoute == "settings" -> R.string.screen_settings
                            currentRoute == "hadith" -> R.string.hadith_library
                            currentRoute == "qibla" -> R.string.qibla_compass
                            else -> R.string.app_name
                        }
                        Text(
                            text = stringResource(titleRes),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    val items = listOf(
                        Triple("dashboard", Icons.Default.Home, R.string.screen_dashboard),
                        Triple("quran", Icons.Default.MenuBook, R.string.screen_quran),
                        Triple("adhkar", Icons.Default.FavoriteBorder, R.string.screen_adhkar),
                        Triple("calendar", Icons.Default.CalendarMonth, R.string.screen_calendar),
                        Triple("settings", Icons.Default.Settings, R.string.screen_settings)
                    )

                    items.forEach { (route, icon, labelRes) ->
                        val selected = currentRoute == route || (route == "quran" && currentRoute.startsWith("quran/"))
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = stringResource(labelRes),
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(labelRes),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                AppNavigation(navController = navController)
            }
        }
    }
}
