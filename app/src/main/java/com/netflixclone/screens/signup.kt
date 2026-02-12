package com.netflixclone.screens


import com.netflixclone.helpers.AuthTokenManager
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.netflixclone.R
import com.netflixclone.network.services.LoginRequest
import com.netflixclone.network.services.TokenResponse
import com.netflixclone.network.models.RetrofitClient
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var authTokenManager: AuthTokenManager  // Helper class to store token

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)
        val signupButton: Button = findViewById(R.id.signup_button)



        authTokenManager = AuthTokenManager(this)

        signupButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            signUpUser(email, password)
        }
    }

    private fun signUpUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val tokenResponse = RetrofitClient.instance.signup(LoginRequest(email, password))
                authTokenManager.saveToken(tokenResponse.token)
                authTokenManager.saveSubscriptionStatus(true)
                val intent = Intent(this@SignupActivity, BottomNavActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: retrofit2.HttpException) {
                e.printStackTrace()
                val errorBody = e.response()?.errorBody()?.string()
                
                var errorMessage = "Signup Failed: ${e.message()}"
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
                Toast.makeText(this@SignupActivity, errorMessage, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@SignupActivity, "Failed to signup: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
