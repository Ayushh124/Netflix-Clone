package com.netflixclone.screens

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class TrackSelectionDialog(private val onQualitySelected: (Quality) -> Unit) : DialogFragment() {

    enum class Quality(val maxBitrate: Int) {
        AUTO(Int.MAX_VALUE),
        HIGH(5_000_000),   // ~5 Mbps (1080p)
        MEDIUM(2_500_000), // ~2.5 Mbps (720p)
        LOW(1_000_000)     // ~1 Mbps (480p)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val qualities = Quality.values()
        val items = qualities.map { it.name }.toTypedArray()

        return AlertDialog.Builder(requireContext())
            .setTitle("Select Quality")
            .setItems(items) { _, which ->
                onQualitySelected(qualities[which])
            }
            .create()
    }
    
    companion object {
        const val TAG = "TrackSelectionDialog"
    }
}
