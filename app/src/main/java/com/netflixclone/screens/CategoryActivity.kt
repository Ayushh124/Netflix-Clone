package com.netflixclone.screens

import com.netflixclone.helpers.AuthTokenManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.netflixclone.data_models.Movie
import com.netflixclone.network.models.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

class CategoryActivity : ComponentActivity() {
    private val categories = listOf("Action", "Sci-Fi", "Thriller", "Superhero", "Western", "Drama", "Romance")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tag = intent.getStringExtra("tag")
        setContent {
            if (tag != null) {
                TagMoviesScreen(tag)
            } else {
                CategoryListScreen(categories)
            }
        }
    }
}

@Composable
fun TagMoviesScreen(tag: String) {
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    val context = LocalContext.current
    
    LaunchedEffect(tag) {
        movies = fetchMoviesByTag(tag, context)
    }

    MaterialTheme(colors = darkColors(background = Color.Black)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Movies tagged: $tag", color = Color.White, modifier = Modifier.padding(8.dp))
            LazyColumn {
                items(movies) { movie ->
                     MovieComponent(movie)
                }
            }
        }
    }
}

@Composable
fun CategoryListScreen(categories: List<String>) {
    // ... (existing implementation) ...
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    MaterialTheme(colors = darkColors(background = Color.Black)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Categories", color = Color.White, modifier = Modifier.padding(8.dp))

            // **Horizontal Scrollable Categories**
            LazyRow(modifier = Modifier.fillMaxWidth()) {
                items(categories) { category ->
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                selectedCategory = category
                                scope.launch {
                                    movies = fetchMoviesByCategory(category, context)
                                }
                            }
                            .background(Color.Gray)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = category, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            selectedCategory?.let {
                Text("Movies in $it", color = Color.White, modifier = Modifier.padding(8.dp))
            }

            LazyColumn {
                items(movies) { movie ->
                    MovieComponent(movie)
                }
            }
        }
    }
}

private suspend fun fetchMoviesByCategory(category: String, context: Context): List<Movie> {
    return try {
        RetrofitClient.instance.getMoviesByCategory(category)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

private suspend fun fetchMoviesByTag(tag: String, context: Context): List<Movie> {
    return try {
        RetrofitClient.instance.getMoviesFiltered(tag = tag)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}