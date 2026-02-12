package com.netflixclone.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.netflixclone.ui.screens.LoginScreen
import com.netflixclone.ui.screens.SignupScreen
import com.netflixclone.ui.screens.HomeScreen
import com.netflixclone.ui.screens.SearchScreen
import com.netflixclone.ui.screens.MovieDetailsScreen
import com.netflixclone.ui.screens.VideoPlayerScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Signup.route) { SignupScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Search.route) { SearchScreen(navController) }
        
        composable(
            route = Screen.MovieDetails.route
        ) { backStackEntry ->
            // movieId is extracted simply by string manipulation or via arguments if defined using navArgument
            // For simplicity in this structure:
             MovieDetailsScreen(navController)
        }

        composable(
            route = Screen.VideoPlayer.route
        ) { backStackEntry ->
             val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
             VideoPlayerScreen(navController, movieId)
        }
    }
}
