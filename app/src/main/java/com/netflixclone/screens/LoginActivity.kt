package com.netflixclone.screens

import com.netflixclone.helpers.AuthTokenManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import retrofit2.HttpException
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.netflixclone.R
import com.netflixclone.network.services.GoogleLoginRequest
import com.netflixclone.network.services.LoginRequest
import com.netflixclone.network.services.TokenResponse
import com.netflixclone.network.models.RetrofitClient
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var authTokenManager: AuthTokenManager
    private lateinit var googleSignInClient: GoogleSignInClient

    // Placeholder for Web Client ID (User needs to replace this)
    private val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID_HERE" 

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        authTokenManager = AuthTokenManager(this)

        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)
        val loginButton: Button = findViewById(R.id.login_button)
        val signupButton: Button = findViewById(R.id.signup_button)
        val googleSignInButton: SignInButton = findViewById(R.id.google_sign_in_button)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                firebaseAuthWithGoogle(idToken)
            } else {
                Toast.makeText(this, "Google Sign In Failed: No ID Token", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.w("LoginActivity", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        // We call our backend API to verify the token and log the user in
        lifecycleScope.launch {
            try {
                val tokenResponse = RetrofitClient.instance.googleLogin(GoogleLoginRequest(idToken))
                handleLoginSuccess(tokenResponse.token, tokenResponse.subscribed)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LoginActivity, "Backend Auth Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        Log.d("LOGIN_DEBUG", "Attempting login with Email: $email")
        lifecycleScope.launch {
            try {
                val tokenResponse = RetrofitClient.instance.login(LoginRequest(email, password))
                Log.d("LOGIN_DEBUG", "Login Successful. Token: ${tokenResponse.token}")
                handleLoginSuccess(tokenResponse.token, tokenResponse.subscribed)
            } catch (e: retrofit2.HttpException) {
                e.printStackTrace()
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("LOGIN_DEBUG", "Login Failed. Code: ${e.code()}, Error: $errorBody")
                
                var errorMessage = "Login Failed: ${e.message()}"
                try {
                    if (!errorBody.isNullOrEmpty()) {
                         val jsonObject = org.json.JSONObject(errorBody)
                         if (jsonObject.has("message")) {
                             errorMessage = jsonObject.getString("message")
                         }
                    }
                } catch (jsonException: Exception) {
                    jsonException.printStackTrace()
                }
                
                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("LOGIN_DEBUG", "Login Error: ${e.message}")
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleLoginSuccess(token: String, isSubscribed: Boolean) {
        authTokenManager.saveToken(token)
        authTokenManager.saveSubscriptionStatus(true) // Default to true
        startActivity(Intent(this, BottomNavActivity::class.java))
        finish()
    }
}

