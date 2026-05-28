package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.ui.MainViewModel
import com.example.ui.screens.*

@Composable
fun AppNavigation(navController: NavHostController, viewModel: MainViewModel) {
    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") { DashboardScreen(viewModel) }
        composable("quran") { QuranScreen(viewModel) }
        composable("hadith") { HadithScreen(viewModel) }
        composable("adhkar") { AdhkarScreen(viewModel) }
        composable("qibla") { QiblaCompassScreen(viewModel) }
        composable("calendar") { CalendarScreen(viewModel) }
        composable("settings") { SettingsScreen(viewModel) }
    }
}
