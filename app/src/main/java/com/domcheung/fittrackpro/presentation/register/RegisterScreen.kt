package com.domcheung.fittrackpro.presentation.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domcheung.fittrackpro.data.model.User

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Register", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") })
        OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Gender") })
        OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height (cm)") })
        OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight (kg)") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.errorMessage?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                val user = User(
                    name = name,
                    age = age.toIntOrNull() ?: 0,
                    gender = gender,
                    height = height.toFloatOrNull() ?: 0f,
                    weight = weight.toFloatOrNull() ?: 0f
                )
                viewModel.registerUser(email, password, user)
            },
            enabled = !uiState.isLoading
        ) {
            Text("Register")
        }

        if (uiState.isSuccess) {
            LaunchedEffect(Unit) {
                onRegisterSuccess()
            }
        }
    }
}