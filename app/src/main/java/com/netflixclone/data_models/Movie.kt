package com.netflixclone.data_models

import com.squareup.moshi.Json

/**
 * Define MovieTag here so Moshi knows how to parse it.
 * This matches the 'tags' object coming from your MySQL/Node.js backend.
 */
data class MovieTag(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name: String = ""
)

/**
 * Main Movie data class that maps the Node.js/MySQL backend response 
 * to the Android UI model.
 */
data class Movie(
    @Json(name = "id") val _id: Int, 
    @Json(name = "title") val _title: String,
    @Json(name = "thumbnail_url") val _posterPath: String?,
    @Json(name = "description") val _overview: String?,
    @Json(name = "created_at") val _releaseDate: String?,
    @Json(name = "category") val category: String?,
    @Json(name = "video_url") val _videoUrl: String?,
    @Json(name = "Tags") val _tags: List<MovieTag>?
) : IMovie {

    override val id: String
        get() = _id.toString()

    override val title: String
        get() = _title
    
    override val posterPath: String?
        get() = _posterPath
    
    override val backdropPath: String?
        get() = _posterPath
    
    override val overview: String
        get() = _overview ?: ""
    
    override val releaseDate: String?
        get() = _releaseDate
    
    override val videoUrl: String?
        get() = _videoUrl

    override val tags: List<String>?
        get() = _tags?.map { it.name }
    
    override val voteAverage: Double
        get() = 0.0
    
    val genreIds: List<Int>
        get() = emptyList()
}

fun Movie.toMediaMovie() =
    Media.Movie(
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