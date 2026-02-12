package com.netflixclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.netflixclone.navigation.Screen
import com.netflixclone.data_models.MovieTag
import com.netflixclone.ui.components.MovieItem
import com.netflixclone.ui.viewmodels.SearchState
import com.netflixclone.ui.viewmodels.SearchViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val searchState by viewModel.searchState.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val selectedTagIds = viewModel.selectedTagIds

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.onSearchQueryChanged(it)
            },
            label = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.DarkGray,
                unfocusedContainerColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tags Row with Reset Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Tags
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(count = tags.size) { index ->
                    val tag = tags[index]
                    val isSelected = viewModel.isTagSelected(tag.id)
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleTagSelection(tag.id) },
                        label = { Text(tag.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.DarkGray,
                            labelColor = Color.White,
                            selectedContainerColor = Color.Red,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            // Reset Button
            if (selectedTagIds.isNotEmpty() || query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        query = ""
                        viewModel.resetFilters()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF444444)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Reset", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results
        when (searchState) {
            is SearchState.Loading -> {
                CircularProgressIndicator(color = Color.Red, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
            }
            is SearchState.Success -> {
                val movies = (searchState as SearchState.Success).movies
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(movies) { movie ->
                        MovieItem(movie = movie, onClick = {
                            navController.navigate(Screen.MovieDetails.createRoute(movie.id))
                        })
                    }
                }
            }
            is SearchState.Error -> {
                Text(text = (searchState as SearchState.Error).message, color = Color.Red)
            }
            else -> {}
        }
    }
}
