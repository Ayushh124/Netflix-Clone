package com.netflixclone.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.netflixclone.data_models.Movie


@Composable
fun MovieComponent(movie: Movie) {
    val context = LocalContext.current
    Row(modifier = Modifier
        .background(Color.White)
        .fillMaxWidth()
        .clickable {
            movie.videoUrl?.let { videoUrl ->
                VideoPlayerActivity.start(context, videoUrl)
            }
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(120.dp),
            painter = rememberAsyncImagePainter(model = movie.posterPath),
            contentDescription = "movie image"
        )
        Text(movie.title)
    }
}

@Preview
@Composable
fun MovieComponentPreview() {
    MovieComponent(
        Movie(
            _id = 1,
            _title = "abc",
            _posterPath = "https://picsum.photos/200/300",
            _overview = "A great movie",
            _releaseDate = "",
            category = "Popular",
            _videoUrl = "",
            _tags = null
        )
    )
}