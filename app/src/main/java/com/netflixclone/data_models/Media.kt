package com.netflixclone.data_models

import com.squareup.moshi.Json

sealed class Media(
    @Json(name = "media_type") val mediaType: MediaType,
) {
    data class Movie(
        @Json(name = "id") override val id: String,
        @Json(name = "title") override val title: String,
        @Json(name = "poster_path") override val posterPath: String?,
        @Json(name = "backdrop_path") override val backdropPath: String?,
        @Json(name = "overview") override val overview: String,
        @Json(name = "release_date") override val releaseDate: String?,
        @Json(name = "vote_average") override val voteAverage: Double,
        @Json(name = "genre_ids") val genreIds: List<Int>,
        @Json(name = "video_url") override val videoUrl: String? = null,
        @Json(name = "tags") override val tags: List<String>? = null
    ) : Media(MediaType.MOVIE), IMovie

    data class Tv(
        @Json(name = "id") override val id: String,
        @Json(name = "name") override val name: String,
        @Json(name = "poster_path") override val posterPath: String?,
        @Json(name = "backdrop_path") override val backdropPath: String?,
        @Json(name = "overview") override val overview: String,
        @Json(name = "first_air_date") override val firstAirDate: String?,
        @Json(name = "vote_average") override val voteAverage: Double,
        @Json(name = "genre_ids") val genreIds: List<Int>,
    ) : Media(MediaType.TV), ITvShow

    data class Person(
        @Json(name = "id") val id: String,
        @Json(name = "name") val name: String,
        @Json(name = "gender") val posterPath: Int,
    ) : Media(MediaType.PERSON)
}

enum class MediaType {
    @Json(name = "movie")
    MOVIE,

    @Json(name = "tv")
    TV,

    @Json(name = "person")
    PERSON,
}