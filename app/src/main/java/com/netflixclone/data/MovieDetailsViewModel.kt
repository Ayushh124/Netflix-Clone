package com.netflixclone.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.netflixclone.data_models.Resource
import com.netflixclone.network.models.MovieDetailsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val apiService: com.netflixclone.network.services.ApiService
) : ViewModel() {
    val details: MutableLiveData<Resource<MovieDetailsResponse>> =
        MutableLiveData(Resource(false, null, null))

    fun fetchMovieDetails(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            details.postValue(details.value!!.copy(isLoading = true))
            try {
                // Use local API
                val response = apiService.getMovieDetails(id)
                details.postValue(details.value!!.copy(isLoading = false, data = response))
            } catch (e: Exception) {
                // Fallback to MediaRepository if local fails? OR stick to local.
                // For now stick to local as we want tags.
                details.postValue(details.value!!.copy(isLoading = false, error = e.message))
            }
        }
    }
}