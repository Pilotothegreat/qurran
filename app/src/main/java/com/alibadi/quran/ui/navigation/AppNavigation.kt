package com.alibadi.quran.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.alibadi.quran.ui.screens.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(
                navController = navController,
                prayerViewModel = koinViewModel(),
                quranViewModel = koinViewModel(),
                hadithViewModel = koinViewModel(),
                settingsViewModel = koinViewModel(),
                calendarViewModel = koinViewModel()
            )
        }
        composable("quran") {
            QuranIndexScreen(
                viewModel = koinViewModel(),
                onSurahClick = { surahNumber ->
                    navController.navigate("quran/$surahNumber")
                }
            )
        }
        composable("quran/{surahNumber}") { backStackEntry ->
            val surahNum = backStackEntry.arguments?.getString("surahNumber")?.toInt() ?: 1
            QuranReaderScreen(
                surahNumber = surahNum,
                viewModel = koinViewModel(),
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("hadith") {
            HadithScreen(
                viewModel = koinViewModel()
            )
        }
        composable("adhkar") {
            AdhkarScreen(
                viewModel = koinViewModel()
            )
        }
        composable("qibla") {
            QiblaCompassScreen(
                viewModel = koinViewModel()
            )
        }
        composable("calendar") {
            CalendarScreen(
                viewModel = koinViewModel()
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = koinViewModel()
            )
        }
    }
}
