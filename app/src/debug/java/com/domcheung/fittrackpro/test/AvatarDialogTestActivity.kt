package com.domcheung.fittrackpro.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.domcheung.fittrackpro.presentation.profile.components.AvatarDialog
import com.domcheung.fittrackpro.presentation.onboarding.defaultAvatarOptions
import com.domcheung.fittrackpro.ui.theme.FitTrackProTheme

class AvatarDialogTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitTrackProTheme {
                var showDialog by remember { mutableStateOf(false) }
                var currentAvatar by remember { mutableStateOf("") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Avatar Dialog Test",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "Current Avatar: $currentAvatar",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = { showDialog = true }
                        ) {
                            Text("Show Avatar Dialog")
                        }

                        if (currentAvatar.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { currentAvatar = "" }
                            ) {
                                Text("Clear Avatar")
                            }
                        }
                    }
                }

                if (showDialog) {
                    AvatarDialog(
                        currentAvatarData = currentAvatar,
                        onAvatarChange = { newAvatarData ->
                            currentAvatar = newAvatarData
                        },
                        onDismiss = { showDialog = false },
                        onSave = {
                            showDialog = false
                        },
                        isLoading = false
                    )
                }
            }
        }
    }
}