package com.netflixclone.network.services

import com.netflixclone.data_models.Movie
import com.netflixclone.network.models.MovieDetailsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.netflixclone.data_models.MovieTag
/* ✅ Request Models */

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val email: String,
    val password: String,
    val name: String
)

data class GoogleLoginRequest(
    val idToken: String
)

/* ✅ Response Model */

data class TokenResponse(
    val token: String,
    val subscribed: Boolean,
    val message: String? = null
)

data class StreamResponse(
    val videoUrl: String,
    val movieId: Int,
    val title: String
)

/* ✅ API Interface */

interface ApiService {

    /* ✅ COROUTINE STYLE AUTH (CRITICAL FIX) */

    @Headers("Content-Type: application/json")
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @Headers("Content-Type: application/json")
    @POST("auth/register")
    suspend fun signup(@Body request: LoginRequest): TokenResponse

    @Headers("Content-Type: application/json")
    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): TokenResponse

    /* ✅ Other APIs (No Change Needed) */

    @GET("tags")
    suspend fun getTags(): List<MovieTag>

    @GET("moviesFiltered")
    suspend fun getMoviesFiltered(
        @Query("tag") tag: String? = null,
        @Query("search") search: String? = null
    ): List<Movie>

    @GET("movies")
    suspend fun getMoviesByTags(
        @Query("tags") tags: String? = null,
        @Query("search") search: String? = null
    ): List<Movie>

    @GET("movies/{id}")
    suspend fun getMovieDetails(@Path("id") id: Int): MovieDetailsResponse

    @GET("stream/{movieId}")
    suspend fun getStreamUrl(@Path("movieId") movieId: String): StreamResponse

    @GET("movies/featured")
    suspend fun getFeaturedMovies(): List<Movie>

    /* ✅ Legacy / Other APIs */

    @GET("movies")
    suspend fun getMovies(): List<Movie>

    @GET("movies/category")
    suspend fun getMoviesByCategory(@Query("category") category: String): List<Movie>

    @GET("movies/list")
    suspend fun getMovieCategories(): List<String>
}
