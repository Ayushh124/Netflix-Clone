package com.netflixclone.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.netflixclone.data_models.Movie

@Composable
fun MovieItem(
    movie: Movie,
    onClick: (Movie) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(120.dp)
            .height(180.dp)
            .clickable { onClick(movie) },
        shape = RoundedCornerShape(4.dp)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(movie.posterPath)
                .crossfade(true)
                .listener(
                    onError = { _, result ->
                        Log.e("MovieItem", "Image load error for ${movie.title}: ${result.throwable.message}")
                        Log.e("MovieItem", "Image URL: ${movie.posterPath}")
                    },
                    onSuccess = { _, _ ->
                        Log.d("MovieItem", "Image loaded successfully for ${movie.title}")
                    }
                )
                .build(),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Red)
                }
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Image",
                        color = Color.White
                    )
                }
            }
        )
    }
}
