package com.netflixclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.netflixclone.navigation.Screen
import com.netflixclone.ui.viewmodels.AuthState
import com.netflixclone.ui.viewmodels.AuthViewModel

@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "SIGN UP", style = MaterialTheme.typography.titleLarge, color = Color.White)
        
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
                    viewModel.signup(email, password)
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
                Text("Register", color = Color.White)
            }
        }
        
        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = (authState as AuthState.Error).message, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
             Text("Already have an account? Login", color = Color.Gray)
        }

        // Navigate on Success
        LaunchedEffect(authState) {
            if (authState is AuthState.Success) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Signup.route) { inclusive = true }
                }
            }
        }
    }
}
