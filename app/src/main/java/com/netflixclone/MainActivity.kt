package com.netflixclone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.netflixclone.navigation.AppNavigation
import com.netflixclone.ui.theme.NetflixCloneTheme
import com.netflixclone.ui.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle OAuth callback from deep link
        handleDeepLink(intent)
        
        setContent {
            NetflixCloneTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }
    
    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data
        
        if (data != null && data.scheme == "netflixclone" && data.host == "auth") {
            val success = data.getQueryParameter("success")
            val session = data.getQueryParameter("session")
            
            Log.d("GitHubAuth", "Deep link received - success: $success, session: $session")
            
            if (success == "true") {
                // GitHub OAuth was successful
                // Verify the session by making an authenticated request
                authViewModel.verifyGitHubSession()
                
                Log.d("GitHubAuth", "Session verification initiated")
            } else {
                Log.e("GitHubAuth", "GitHub OAuth failed or was cancelled")
            }
        }
    }
}
