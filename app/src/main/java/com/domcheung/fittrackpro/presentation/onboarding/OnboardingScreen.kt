package com.domcheung.fittrackpro.presentation.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domcheung.fittrackpro.presentation.onboarding.AvatarSelectionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onComplete: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val onboardingData by viewModel.onboardingData.collectAsState()
    var showAvatarDialog by remember { mutableStateOf(false) }

    // Handle completion
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onComplete()
        }
    }

    // Handle skip
    LaunchedEffect(uiState.isSkipped) {
        if (uiState.isSkipped) {
            onSkip()
        }
    }

    // Handle loading
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome to FitTrack Pro") },
                actions = {
                    TextButton(onClick = { viewModel.skipOnboarding() }) {
                        Text("Skip")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = viewModel.getProgress(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // Page indicator
            Text(
                text = "Step ${currentPage + 1} of ${viewModel.totalPages}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Content pager
            val pagerState = rememberPagerState { viewModel.totalPages }

            // Sync pager state with current page
            LaunchedEffect(currentPage) {
                if (pagerState.currentPage != currentPage) {
                    pagerState.animateScrollToPage(currentPage)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false // We control navigation programmatically
            ) { page ->
                when (page) {
                    0 -> AvatarSelectionPage(
                        onboardingData = onboardingData,
                        onAvatarClick = { showAvatarDialog = true }
                    )
                    1 -> PhysicalDataPage(
                        onboardingData = onboardingData,
                        onHeightChange = viewModel::updateHeight,
                        onCurrentWeightChange = viewModel::updateCurrentWeight,
                        onTargetWeightChange = viewModel::updateTargetWeight
                    )
                    2 -> GoalsPage(
                        onboardingData = onboardingData,
                        onExperienceChange = viewModel::updateExperienceLevel,
                        onGoalChange = viewModel::updatePrimaryGoal,
                        onFrequencyChange = viewModel::updateWorkoutFrequency
                    )
                }
            }

            // Error message
            uiState.pageError?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Previous button
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = { viewModel.previousPage() },
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(120.dp))
                }

                // Next/Complete button
                if (currentPage < viewModel.totalPages - 1) {
                    Button(
                        onClick = { viewModel.nextPage() },
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = { viewModel.completeOnboarding() },
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text("Complete")
                    }
                }
            }
        }
    }

    // Avatar selection dialog
    if (showAvatarDialog) {
        AvatarSelectionDialog(
            onDismiss = { showAvatarDialog = false },
            onAvatarSelected = { avatar ->
                viewModel.updateSelectedAvatar(avatar)
                showAvatarDialog = false
            },
            currentlySelected = onboardingData.selectedAvatar
        )
    }
}

@Composable
private fun AvatarSelectionPage(
    onboardingData: OnboardingData,
    onAvatarClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "👋 Let's Get Started!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Choose an avatar to personalize your profile",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Avatar selection button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable { onAvatarClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (onboardingData.selectedAvatar != null) {
                    Text(
                        text = onboardingData.selectedAvatar.emoji,
                        fontSize = MaterialTheme.typography.displayLarge.fontSize
                    )
                    Text(
                        text = onboardingData.selectedAvatar.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text(
                        text = "🎭",
                        fontSize = MaterialTheme.typography.displayLarge.fontSize
                    )
                    Text(
                        text = "Click to choose avatar",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Text(
            text = "Avatar is optional - you can skip this step",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun PhysicalDataPage(
    onboardingData: OnboardingData,
    onHeightChange: (String) -> Unit,
    onCurrentWeightChange: (String) -> Unit,
    onTargetWeightChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📏 Your Physical Data",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "This helps us track your progress and set realistic goals",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Height input
        OutlinedTextField(
            value = onboardingData.height,
            onValueChange = onHeightChange,
            label = { Text("Height") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. 175") },
            suffix = { Text("cm") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current weight input
        OutlinedTextField(
            value = onboardingData.currentWeight,
            onValueChange = onCurrentWeightChange,
            label = { Text("Current Weight") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. 75") },
            suffix = { Text("kg") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Target weight input
        OutlinedTextField(
            value = onboardingData.targetWeight,
            onValueChange = onTargetWeightChange,
            label = { Text("Target Weight") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. 70") },
            suffix = { Text("kg") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
        )
    }
}

@Composable
private fun GoalsPage(
    onboardingData: OnboardingData,
    onExperienceChange: (ExperienceLevel) -> Unit,
    onGoalChange: (FitnessGoal) -> Unit,
    onFrequencyChange: (WorkoutFrequency) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎯 Your Fitness Goals",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Let's tailor your workout plan to your needs",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Experience level
        Text(
            text = "Fitness Experience",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        ExperienceLevel.values().forEach { level ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = onboardingData.experienceLevel == level,
                    onClick = { onExperienceChange(level) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = level.displayName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Primary goal
        Text(
            text = "Primary Goal",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        FitnessGoal.values().forEach { goal ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = onboardingData.primaryGoal == goal,
                    onClick = { onGoalChange(goal) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = goal.displayName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Workout frequency
        Text(
            text = "Preferred Workout Frequency",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        WorkoutFrequency.values().forEach { frequency ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = onboardingData.workoutFrequency == frequency,
                    onClick = { onFrequencyChange(frequency) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = frequency.displayName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
