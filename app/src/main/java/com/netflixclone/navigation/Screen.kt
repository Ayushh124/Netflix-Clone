package com.netflixclone.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Search : Screen("search")
    object Downloads : Screen("downloads")
    object More : Screen("more")
    object MovieDetails : Screen("movie_details/{movieId}") {
        fun createRoute(movieId: String) = "movie_details/$movieId"
    }
    object VideoPlayer : Screen("video_player/{movieId}") {
        fun createRoute(movieId: String) = "video_player/$movieId"
    }
}
