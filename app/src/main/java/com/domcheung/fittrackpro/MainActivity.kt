package com.domcheung.fittrackpro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.domcheung.fittrackpro.presentation.register.RegisterScreen
import com.domcheung.fittrackpro.ui.theme.FitTrackProTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hilt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // using App theme
            FitTrackProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    RegisterScreen(
                        onRegisterSuccess = {
                            Toast.makeText(context, "Registration Successful!", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }
    }
}