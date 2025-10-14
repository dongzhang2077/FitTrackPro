package com.domcheung.fittrackpro.presentation.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.domcheung.fittrackpro.presentation.onboarding.AvatarSelectionDialog
import com.domcheung.fittrackpro.presentation.onboarding.defaultAvatarOptions
import com.domcheung.fittrackpro.presentation.onboarding.DefaultAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNameDialog(
    currentName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf(currentName) }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Name",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = it.isBlank() || it.length < 2
                    },
                    label = { Text("Your Name") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Name must be at least 2 characters") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank() && name.length >= 2) {
                                onNameChange(name)
                                onSave()
                            }
                        },
                        enabled = !isLoading && name.isNotBlank() && name.length >= 2,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarDialog(
    currentAvatarData: String,
    onAvatarChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    isLoading: Boolean = false
) {
    // Parse current avatar data
    val currentAvatar = parseAvatarData(currentAvatarData)

    AvatarSelectionDialog(
        onDismiss = onDismiss,
        onAvatarSelected = { selectedAvatar ->
            val avatarString = "${selectedAvatar.id}:${selectedAvatar.emoji}:${selectedAvatar.color.value.toString()}"
            onAvatarChange(avatarString)
            onSave()
        },
        currentlySelected = currentAvatar
    )
}

    // Parse avatar data string
    private fun parseAvatarData(avatarString: String): DefaultAvatar? {
    if (avatarString.isBlank()) return null

    return try {
        val parts = avatarString.split(":")
        if (parts.size >= 3) {
            val id = parts[0]
            val emoji = parts[1]
            val colorValue = parts[2].toLongOrNull(16) ?: 0xFF000000L
            val color = Color(colorValue)

            // Find matching default avatar
            defaultAvatarOptions.find { it.id == id }
                ?.copy(
                    emoji = emoji,
                    color = color
                )
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSettingsDialog(
    currentWeight: String,
    targetWeight: String,
    initialWeight: String,
    onCurrentWeightChange: (String) -> Unit,
    onTargetWeightChange: (String) -> Unit,
    onInitialWeightChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    isLoading: Boolean = false
) {
    fun sanitizeInput(weight: String): String {
        var decimalFound = false
        val sanitized = buildString {
            weight.forEach { char ->
                when {
                    char.isDigit() -> append(char)
                    char == '.' && !decimalFound -> {
                        append(char)
                        decimalFound = true
                    }
                }
            }
        }.trim()
        return sanitized.trimEnd { it == '.' }
    }

    var currentW by remember { mutableStateOf(sanitizeInput(currentWeight)) }
    var targetW by remember { mutableStateOf(sanitizeInput(targetWeight)) }
    var initialW by remember { mutableStateOf(sanitizeInput(initialWeight)) }
    var currentError by remember { mutableStateOf(false) }
    var targetError by remember { mutableStateOf(false) }

    fun validateWeight(weight: String): Boolean {
        val sanitized = sanitizeInput(weight)
        return sanitized.isNotEmpty() && sanitized.toFloatOrNull() != null && sanitized.toFloat() > 0
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Weight Goals",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Initial Weight
                OutlinedTextField(
                    value = initialW,
                    onValueChange = {
                        initialW = sanitizeInput(it)
                    },
                    label = { Text("Initial Weight") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g. 80") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current Weight
                OutlinedTextField(
                    value = currentW,
                    onValueChange = {
                        currentW = sanitizeInput(it)
                        currentError = !validateWeight(currentW)
                    },
                    label = { Text("Current Weight") },
                    isError = currentError,
                    supportingText = if (currentError) {
                        { Text("Please enter a valid weight") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g. 75") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Target Weight
                OutlinedTextField(
                    value = targetW,
                    onValueChange = {
                        targetW = sanitizeInput(it)
                        targetError = !validateWeight(targetW)
                    },
                    label = { Text("Target Weight") },
                    isError = targetError,
                    supportingText = if (targetError) {
                        { Text("Please enter a valid weight") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g. 70") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            if (!currentError && !targetError && validateWeight(currentW) && validateWeight(targetW)) {
                                onCurrentWeightChange(currentW)
                                onTargetWeightChange(targetW)
                                onInitialWeightChange(initialW)
                                onSave()
                            }
                        },
                        enabled = !isLoading && !currentError && !targetError && validateWeight(currentW) && validateWeight(targetW),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
