package com.netflixclone.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netflixclone.data.repository.FeedRepository
import com.netflixclone.network.models.MovieDetailsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailsState>(DetailsState.Loading)
    val uiState: StateFlow<DetailsState> = _uiState

    init {
        val movieId = savedStateHandle.get<String>("movieId")
        if (movieId != null) {
            fetchDetails(movieId)
        }
    }

    fun fetchDetails(id: String) {
        viewModelScope.launch {
            _uiState.value = DetailsState.Loading
            val result = feedRepository.getMovieDetails(id)
            if (result.isSuccess) {
                _uiState.value = DetailsState.Success(result.getOrNull()!!)
            } else {
                _uiState.value = DetailsState.Error(result.exceptionOrNull()?.message ?: "Unknown Error")
            }
        }
    }
}

sealed class DetailsState {
    object Loading : DetailsState()
    data class Success(val details: MovieDetailsResponse) : DetailsState()
    data class Error(val message: String) : DetailsState()
}
