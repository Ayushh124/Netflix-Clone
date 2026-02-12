package com.netflixclone.screens

import com.netflixclone.helpers.AuthTokenManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.netflixclone.data_models.Movie
import com.netflixclone.network.models.RetrofitClient
import com.netflixclone.screens.SubscriptionActivity
import retrofit2.Call
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class FeedFragment : BottomNavFragment() {
    lateinit var rootView: ComposeView

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        rootView = ComposeView(requireContext()).apply {
            setContent {
                var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
                MaterialTheme(colors = darkColors(background = Color.Black)) {
                    val context = LocalContext.current

//                    FeedScreen()
                    LaunchedEffect(Unit) {
                        val authTokenManager = AuthTokenManager(context)

                        if (!authTokenManager.isUserSubscribed()) {
                            Toast.makeText(context, "You need a subscription to watch movies!", Toast.LENGTH_LONG).show()
                            startActivity(Intent(context, SubscriptionActivity::class.java))
                            parentFragmentManager.popBackStack()
                        } else {
                            movies = fetchMovies(context)
                        }
                    }
                    Column {
                        Button(
                            onClick = {
                                val intent=Intent(context, CategoryActivity::class.java)
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Search by category")
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                        LazyColumn {
                            items(movies) { movie ->
                                MovieComponent(movie)
                            }
                        }
                    }

                }
            }
        }
        return rootView
    }


    override fun onFirstDisplay() {
    }
}
suspend fun fetchMovies(context: Context): List<Movie> {
    return try {
        RetrofitClient.instance.getMovies()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}
