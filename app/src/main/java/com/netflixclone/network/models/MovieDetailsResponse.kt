package com.netflixclone.network.models

import com.netflixclone.data_models.IMovie
import com.netflixclone.data_models.Movie
import com.squareup.moshi.Json

data class MovieDetailsResponse(
        @Json(name = "id") override val id: String,
        @Json(name = "title") override val title: String,
        @Json(name = "poster_path") override val posterPath: String?,
        @Json(name = "backdrop_path") override val backdropPath: String?,
        @Json(name = "overview") override val overview: String,
        @Json(name = "release_date") override val releaseDate: String?,
        @Json(name = "vote_average") override val voteAverage: Double,
        @Json(name = "runtime") val runtime: Int? = null,
        @Json(name = "similar") val similar: PageResponse<Movie>? = null,
        @Json(name = "videos") val videos: VideosResponse? = null,
        @Json(name = "video_url") override val videoUrl: String? = null,
        @Json(name = "Tags") val tagsList: List<com.netflixclone.data_models.MovieTag>? = null
): IMovie {
    override val tags: List<String>?
        get() = tagsList?.map { it.name }
}