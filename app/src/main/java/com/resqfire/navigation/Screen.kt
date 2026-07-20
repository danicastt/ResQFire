package com.resqfire.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Map : Screen("map")
    object EventList : Screen("event_list")
    object AddEvent : Screen("add_event")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object Leaderboard : Screen("leaderboard")
    object Profile : Screen("profile")
}
