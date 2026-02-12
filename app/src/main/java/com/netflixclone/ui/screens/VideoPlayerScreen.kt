package com.netflixclone.ui.screens

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.netflixclone.network.services.ApiService
import com.netflixclone.network.services.StreamResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class) 
@Composable
fun VideoPlayerScreen(
    navController: NavController,
    movieId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var videoUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showQualitySheet by remember { mutableStateOf(false) }
    var availableQualities by remember { mutableStateOf<List<QualityOption>>(emptyList()) }
    var selectedQuality by remember { mutableStateOf<String?>(null) }
    
    // Track selector for adaptive streaming
    var trackSelector: DefaultTrackSelector? by remember { mutableStateOf(null) }
    var exoPlayer: ExoPlayer? by remember { mutableStateOf(null) }
    
    // Inject ApiService through viewModel
    val viewModel: VideoPlayerViewModel = hiltViewModel()
    
    // Fetch video URL on first composition
    LaunchedEffect(movieId) {
        scope.launch {
            try {
                Log.d("VideoPlayer", "Fetching stream URL for movie ID: $movieId")
                val response = viewModel.getStreamUrl(movieId)
                videoUrl = response.videoUrl
                isLoading = false
                Log.d("VideoPlayer", "Got video URL: ${response.videoUrl}")
            } catch (e: Exception) {
                Log.e("VideoPlayer", "Failed to fetch stream URL", e)
                errorMessage = e.message ?: "Failed to load video"
                isLoading = false
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            videoUrl != null -> {
                // Create ExoPlayer with adaptive track selection
                val player = remember(videoUrl) {
                    // Configure adaptive track selection
                    val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory()
                    val selector = DefaultTrackSelector(context, adaptiveTrackSelectionFactory).apply {
                        parameters = buildUponParameters()
                            .setMaxVideoSizeSd()
                            .setPreferredVideoMimeType(MimeTypes.VIDEO_H264)
                            .build()
                    }
                    trackSelector = selector
                    
                    ExoPlayer.Builder(context)
                        .setTrackSelector(selector)
                        .build()
                        .apply {
                            Log.d("VideoPlayer", "Creating ExoPlayer with adaptive HLS")
                            
                            val dataSourceFactory = DefaultHttpDataSource.Factory()
                                .setAllowCrossProtocolRedirects(true)
                                .setUserAgent("NetflixClone/1.0")
                            
                            val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                                .setAllowChunklessPreparation(true)
                                .createMediaSource(MediaItem.fromUri(videoUrl!!))
                            
                            // Add listener for debugging and track changes
                            addListener(object : Player.Listener {
                                override fun onPlaybackStateChanged(state: Int) {
                                    val stateString = when (state) {
                                        Player.STATE_IDLE -> "IDLE"
                                        Player.STATE_BUFFERING -> "BUFFERING"
                                        Player.STATE_READY -> "READY"
                                        Player.STATE_ENDED -> "ENDED"
                                        else -> "UNKNOWN"
                                    }
                                    Log.d("VideoPlayer", "Playback state: $stateString")
                                }
                                
                                override fun onPlayerError(error: PlaybackException) {
                                    Log.e("VideoPlayer", "Playback error: ${error.message}", error)
                                }
                                
                                override fun onIsPlayingChanged(isPlaying: Boolean) {
                                    Log.d("VideoPlayer", "Is playing: $isPlaying")
                                }
                                
                                override fun onTracksChanged(tracks: Tracks) {
                                    Log.d("VideoPlayer", "Tracks changed, extracting qualities...")
                                    availableQualities = extractQualitiesFromTracks(tracks)
                                    Log.d("VideoPlayer", "Available qualities: ${availableQualities.size}")
                                }
                            })
                            
                            setMediaSource(mediaSource)
                            prepare()
                            playWhenReady = true
                            
                            Log.d("VideoPlayer", "ExoPlayer prepared with adaptive HLS")
                        }.also { exoPlayer = it }
                }
                
                DisposableEffect(Unit) {
                    onDispose {
                        player.release()
                        exoPlayer = null
                        trackSelector = null
                        Log.d("VideoPlayer", "ExoPlayer released")
                    }
                }
                
                // Player UI
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = {
                            PlayerView(context).apply {
                                this.player = player
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Settings gear icon button
                    FloatingActionButton(
                        onClick = { showQualitySheet = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        containerColor = Color.Black.copy(alpha = 0.7f),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Quality Settings"
                        )
                    }
                }
                
                // Quality selection bottom sheet
                if (showQualitySheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showQualitySheet = false },
                        containerColor = Color(0xFF1C1C1C),
                        contentColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Video Quality",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            LazyColumn {
                                // Auto quality option
                                item {
                                    QualityOptionItem(
                                        label = "Auto (Recommended)",
                                        isSelected = selectedQuality == null,
                                        onClick = {
                                            selectedQuality = null
                                            enableAutoQuality(trackSelector)
                                            showQualitySheet = false
                                            Log.d("VideoPlayer", "Auto quality enabled")
                                        }
                                    )
                                }
                                
                                // Available quality options
                                items(availableQualities) { quality ->
                                    QualityOptionItem(
                                        label = quality.label,
                                        isSelected = selectedQuality == quality.label,
                                        onClick = {
                                            selectedQuality = quality.label
                                            selectManualQuality(trackSelector, quality.trackIndex, quality.groupIndex)
                                            showQualitySheet = false
                                            Log.d("VideoPlayer", "Manual quality selected: ${quality.label}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data class to hold quality information
 */
data class QualityOption(
    val label: String,
    val trackIndex: Int,
    val groupIndex: Int,
    val bitrate: Int
)

/**
 * Composable for quality option item in the bottom sheet
 */
@Composable
fun QualityOptionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) Color.Red else Color.White
        )
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Selected",
                tint = Color.Red
            )
        }
    }
}

/**
 * Extract quality options from HLS tracks
 */
@androidx.annotation.OptIn(UnstableApi::class)
fun extractQualitiesFromTracks(tracks: Tracks): List<QualityOption> {
    val qualities = mutableListOf<QualityOption>()
    
    for (groupIndex in 0 until tracks.groups.size) {
        val trackGroup = tracks.groups[groupIndex]
        
        // Only process video tracks
        if (trackGroup.type == C.TRACK_TYPE_VIDEO) {
            for (trackIndex in 0 until trackGroup.length) {
                val format = trackGroup.getTrackFormat(trackIndex)
                val height = format.height
                val bitrate = format.bitrate
                
                if (height > 0) {
                    val label = when {
                        height >= 2160 -> "4K (${height}p)"
                        height >= 1080 -> "1080p"
                        height >= 720 -> "720p"
                        height >= 480 -> "480p"
                        height >= 360 -> "360p"
                        else -> "${height}p"
                    }
                    
                    qualities.add(
                        QualityOption(
                            label = label,
                            trackIndex = trackIndex,
                            groupIndex = groupIndex,
                            bitrate = bitrate
                        )
                    )
                }
            }
        }
    }
    
    // Sort by bitrate descending (highest quality first)
    return qualities.sortedByDescending { it.bitrate }.distinctBy { it.label }
}

/**
 * Enable automatic quality selection (adaptive streaming)
 */
@androidx.annotation.OptIn(UnstableApi::class)
fun enableAutoQuality(trackSelector: DefaultTrackSelector?) {
    trackSelector?.let { selector ->
        selector.parameters = selector.buildUponParameters()
            .clearVideoSizeConstraints()
            .clearViewportSizeConstraints()
            .setMaxVideoSizeSd() // Remove size constraints for adaptive
            .build()
        
        Log.d("VideoPlayer", "Auto quality enabled")
    }
}

/**
 * Select manual quality by disabling adaptive mode
 */
@androidx.annotation.OptIn(UnstableApi::class)
fun selectManualQuality(
    trackSelector: DefaultTrackSelector?,
    trackIndex: Int,
    groupIndex: Int
) {
    trackSelector?.let { selector ->
        val parametersBuilder = selector.buildUponParameters()
        
        // Create override for specific track
        val override = TrackSelectionOverride(
            selector.currentMappedTrackInfo?.getTrackGroups(groupIndex)?.get(0)!!,
            listOf(trackIndex)
        )
        
        selector.parameters = parametersBuilder
            .clearVideoSizeConstraints()
            .clearViewportSizeConstraints()
            .addOverride(override)
            .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)
            .build()
        
        Log.d("VideoPlayer", "Manual quality selected: track=$trackIndex, group=$groupIndex")
    }
}

// ViewModel to handle API calls
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val apiService: ApiService
) : androidx.lifecycle.ViewModel() {
    
    suspend fun getStreamUrl(movieId: String): StreamResponse {
        return apiService.getStreamUrl(movieId)
    }
}
