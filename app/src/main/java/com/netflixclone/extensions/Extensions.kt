package com.netflixclone.extensions

import com.netflixclone.data_models.Media
import com.netflixclone.data_models.Movie

fun Movie.toMediaMovie(): Media.Movie {
    return Media.Movie(
        id = id,
        title = title,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        genreIds = genreIds,
        videoUrl = videoUrl,
        tags = tags
    )
}
