package com.resqfire.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.resqfire.ui.screens.*
import com.resqfire.viewmodel.MainViewModel

@Composable
fun ResQFireNavGraph(navController: NavHostController, viewModel: MainViewModel) {
    val isLoggedIn = viewModel.isLoggedIn

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn.value) Screen.Map.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                viewModel = viewModel,
                onNavigateToAdd = { navController.navigate(Screen.AddEvent.route) },
                onNavigateToList = { navController.navigate(Screen.EventList.route) },
                onNavigateToLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onEventClick = { eventId -> navController.navigate(Screen.EventDetail.createRoute(eventId)) }
            )
        }

        composable(Screen.AddEvent.route) {
            AddEventScreen(
                viewModel = viewModel,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EventList.route) {
            EventListScreen(
                viewModel = viewModel,
                onEventClick = { eventId -> navController.navigate(Screen.EventDetail.createRoute(eventId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EventDetail.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(
                viewModel = viewModel,
                eventId = eventId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
