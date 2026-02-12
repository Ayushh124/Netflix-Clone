@file:OptIn(ExperimentalMaterial3Api::class)

package com.netflixclone.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.netflixclone.BuildConfig
import com.netflixclone.R
import com.netflixclone.navigation.Screen
import com.netflixclone.ui.viewmodels.AuthState
import com.netflixclone.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val authState by viewModel.authState.collectAsState()
    
    // Email validation function
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // Validate on change
    fun validateEmail(value: String) {
        emailError = when {
            value.isEmpty() -> "Email is required"
            !isValidEmail(value) -> "Invalid email format"
            else -> null
        }
    }
    
    fun validatePassword(value: String) {
        passwordError = when {
            value.isEmpty() -> "Password is required"
            value.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
    
    val isFormValid = email.isNotEmpty() && 
                      password.isNotEmpty() && 
                      emailError == null && 
                      passwordError == null &&
                      isValidEmail(email) &&
                      password.length >= 6

    /* ✅ Use Client ID from strings.xml */
    val webClientId = stringResource(R.string.web_client_id)

    /* ✅ Create Google Sign-In Options ONCE */
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }

    /* ✅ Create client ONCE */
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    /* ✅ Activity Result Launcher */
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            Log.d("GoogleSignIn", "Token: $idToken")

            if (idToken != null) {
                viewModel.googleLogin(idToken)
            }

        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Sign-in failed", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "NETFLIX",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                validateEmail(it)
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null,
            supportingText = {
                if (emailError != null) {
                    Text(text = emailError!!, color = Color.Red)
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedLabelColor = Color.Gray,
                unfocusedLabelColor = Color.Gray,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Red,
                unfocusedIndicatorColor = Color.Gray,
                errorIndicatorColor = Color.Red,
                errorLabelColor = Color.Red,
                errorTextColor = Color.White,
                errorCursorColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                validatePassword(it)
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError != null,
            supportingText = {
                if (passwordError != null) {
                    Text(text = passwordError!!, color = Color.Red)
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedLabelColor = Color.Gray,
                unfocusedLabelColor = Color.Gray,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Red,
                unfocusedIndicatorColor = Color.Gray,
                errorIndicatorColor = Color.Red,
                errorLabelColor = Color.Red,
                errorTextColor = Color.White,
                errorCursorColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { 
                validateEmail(email)
                validatePassword(password)
                if (isFormValid) {
                    viewModel.login(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                disabledContainerColor = Color.Gray
            )
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign In", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /* ✅ Google Sign-In Button */
        Button(
            onClick = {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo_placeholder),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign in with Google", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        /* ✅ GitHub Sign-In Button */
        Button(
            onClick = {
                viewModel.launchGitHubLogin(BuildConfig.BASE_URL)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E))
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_github_logo),
                contentDescription = "GitHub Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign in with GitHub", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        /* ✅ Signup Link */
        TextButton(
            onClick = { navController.navigate(Screen.Signup.route) }
        ) {
            Text(
                text = "New to Netflix? Sign up now.",
                color = Color.White
            )
        }

        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = Color.Red
            )
        }

        /* ✅ Handle GitHub OAuth - Open Chrome Custom Tab */
        LaunchedEffect(authState) {
            if (authState is AuthState.GitHubAuthRequired) {
                val url = (authState as AuthState.GitHubAuthRequired).url
                
                Log.d("GitHubAuth", "Opening GitHub OAuth URL: $url")
                
                try {
                    // Use Chrome Custom Tabs for seamless OAuth experience
                    val customTabsIntent = CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .build()
                    
                    customTabsIntent.launchUrl(context, Uri.parse(url))
                } catch (e: Exception) {
                    Log.e("GitHubAuth", "Failed to open Chrome Custom Tab", e)
                    
                    // Fallback to regular browser
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            }
        }

        /* ✅ Navigate on Success */
        LaunchedEffect(authState) {
            if (authState is AuthState.Success) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
    }
}
