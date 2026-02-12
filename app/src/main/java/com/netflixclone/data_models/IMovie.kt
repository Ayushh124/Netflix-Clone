package com.netflixclone.data_models

interface IMovie {
    val id: String
    val title: String
    val posterPath: String?
    val backdropPath: String?
    val overview: String
    val releaseDate: String?
    val voteAverage: Double
    val videoUrl: String?
    val tags: List<String>?
}