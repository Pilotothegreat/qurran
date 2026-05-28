package com.example.ui.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.ui.MainViewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import org.koin.androidx.compose.koinViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val viewModel: MainViewModel = koinViewModel()
    
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()
    val amoled by viewModel.amoledMode.collectAsState()
    val simpleMode by viewModel.simpleMode.collectAsState()
    
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
        amoled = amoled
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        if (currentRoute != "dashboard") {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(if (simpleMode) 46.dp else 38.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(if (simpleMode) 26.dp else 20.dp)
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = if (simpleMode) "سَبِيل الرَّشَاد ☪" else "اللَّيَالِي وَاللَّبِيبُ ☪",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontSize = if (simpleMode) 24.sp else 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate("calendar") },
                            modifier = Modifier.size(if (simpleMode) 52.dp else 40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = if (currentRoute == "calendar") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(if (simpleMode) 28.dp else 22.dp)
                            )
                        }
                        IconButton(
                            onClick = { navController.navigate("settings") },
                            modifier = Modifier.size(if (simpleMode) 52.dp else 40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = if (currentRoute == "settings") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(if (simpleMode) 28.dp else 22.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                val iconSize = if (simpleMode) 28.dp else 22.dp
                val labelSize = if (simpleMode) 13.sp else 10.sp
                
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    val items = listOf(
                        Triple("dashboard", Icons.Default.Home, "الرئيسية"),
                        Triple("quran", Icons.Default.Book, "القرآن"),
                        Triple("hadith", Icons.Default.MenuBook, "الحديث"),
                        Triple("adhkar", Icons.Default.Spa, "الأذكار"),
                        Triple("qibla", Icons.Default.CompassCalibration, "القبلة")
                    )

                    items.forEach { (route, icon, label) ->
                        val selected = currentRoute == route
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
                                    contentDescription = label,
                                    tint = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(iconSize)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = labelSize,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                AppNavigation(navController = navController, viewModel = viewModel)
            }
        }
    }
}
