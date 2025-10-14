package com.domcheung.fittrackpro.presentation.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domcheung.fittrackpro.data.model.PersonalRecord
import com.domcheung.fittrackpro.ui.theme.HandDrawnShapes
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredData by viewModel.filteredData.collectAsState()
    val weeklyGoal by viewModel.weeklyWorkoutGoal.collectAsState()

    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            ProgressHeader()
        }

        item {
            TimeRangeSelector(
                selectedRange = uiState.selectedTimeRange,
                onRangeSelected = { viewModel.changeTimeRange(it) }
            )
        }

        item {
            when (uiState.selectedTimeRange) {
                ProgressTimeRange.WEEK -> WeeklyActivityCard(
                    weeklyData = filteredData.weeklyActivity,
                    weeklyGoal = weeklyGoal,
                    isLoading = uiState.isLoading
                )
                ProgressTimeRange.MONTH -> MonthlyProgressCard(
                    monthlyData = filteredData.monthlyProgress,
                    isLoading = uiState.isLoading
                )
                ProgressTimeRange.YEAR -> YearlyProgressCard(
                    yearlyData = filteredData.yearlyProgress,
                    isLoading = uiState.isLoading
                )
            }
        }

        item {
            PersonalRecordsCard(
                personalRecords = filteredData.personalRecords,
                recentPersonalRecords = filteredData.recentPersonalRecords,
                isLoading = uiState.isLoading
            )
        }

        item {
            AchievementStatsCard(
                achievementStats = viewModel.getAchievementStats(),
                isLoading = uiState.isLoading
            )
        }

        item {
            StatisticsSummaryCard(
                statistics = filteredData.statistics,
                isLoading = uiState.isLoading
            )
        }
    }
}

@Composable
private fun ProgressHeader() {
    Column {
        Text(
            text = "📊 Your Progress",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Track your fitness journey",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TimeRangeSelector(
    selectedRange: ProgressTimeRange,
    onRangeSelected: (ProgressTimeRange) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProgressTimeRange.values().forEach { range ->
                FilterChip(
                    onClick = { onRangeSelected(range) },
                    label = { Text(range.label) },
                    selected = selectedRange == range,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WeeklyActivityCard(
    weeklyData: List<WeeklyActivityData>,
    weeklyGoal: Int,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📈 This Week's Training",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(7) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyData.forEach { day ->
                        WeeklyActivityItem(
                            day = day.day,
                            dayOfMonth = day.dayOfMonth,
                            isCompleted = day.isCompleted,
                            isToday = day.isToday,
                            isPast = day.isPast
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val completedCount = weeklyData.count { it.isCompleted }
                val totalDays = weeklyData.size
                Text(
                    text = "Completed: $completedCount/$weeklyGoal this week",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeeklyActivityItem(
    day: String,
    dayOfMonth: Int,
    isCompleted: Boolean,
    isToday: Boolean,
    isPast: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primaryContainer
                        isPast -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.outline
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else if (isToday) {
                Text(
                    text = dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPast)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = day,
            style = MaterialTheme.typography.bodySmall,
            color = when {
                isCompleted -> MaterialTheme.colorScheme.primary
                isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun PersonalRecordsCard(
    personalRecords: List<PersonalRecord>,
    recentPersonalRecords: List<PersonalRecord>,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🏅 Personal Records",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    if (it < 2) Spacer(modifier = Modifier.height(8.dp))
                }
            } else if (personalRecords.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No personal records yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Start working out to set your first records!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(personalRecords) { record ->
                        PersonalRecordItem(record = record)
                    }
                }

                if (recentPersonalRecords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🔥 Recent achievements: ${recentPersonalRecords.size} in the last 30 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalRecordItem(record: PersonalRecord) {
    Card(
        modifier = Modifier.width(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = record.exerciseName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(java.util.Date(record.achievedAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${record.weight}kg × ${record.reps}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = record.recordType.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementStatsCard(
    achievementStats: AchievementStats,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎖️ Your Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(60.dp, 40.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AchievementStatItem(
                        icon = Icons.Default.LocalFireDepartment,
                        value = "${achievementStats.currentStreak}",
                        label = "Day Streak",
                        color = MaterialTheme.colorScheme.error
                    )

                    AchievementStatItem(
                        icon = Icons.Default.EmojiEvents,
                        value = "${achievementStats.totalPersonalRecords}",
                        label = "Total PRs",
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    AchievementStatItem(
                        icon = Icons.Default.Timeline,
                        value = "${achievementStats.averageCompletion.toInt()}",
                        label = "Completion (%)",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (achievementStats.recentPersonalRecords > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🎉 ${achievementStats.recentPersonalRecords} new personal records this month!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatisticsSummaryCard(
    statistics: com.domcheung.fittrackpro.data.repository.WorkoutStatistics?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📈 Overall Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                repeat(2) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp, 40.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }
                    if (row == 0) Spacer(modifier = Modifier.height(16.dp))
                }
            } else if (statistics == null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No workout data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Complete your first workout to see statistics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatisticItem(
                        label = "Total Workouts",
                        value = "${statistics.totalWorkouts}",
                        color = MaterialTheme.colorScheme.primary
                    )

                    StatisticItem(
                        label = "Total Volume (k kg)",
                        value = "${(statistics.totalVolumeLifted / 1000).toInt()}",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatisticItem(
                        label = "Avg Duration (min)",
                        value = "${(statistics.averageWorkoutDuration / 60000)}",
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    StatisticItem(
                        label = "Completion Rate (%)",
                        value = "${statistics.averageCompletionRate.toInt()}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MonthlyProgressCard(
    monthlyData: List<MonthlyProgressData>,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📊 Monthly Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    monthlyData.forEach { data ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .height((data.workoutCount * 10).dp)
                                    .width(20.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(text = data.month)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearlyProgressCard(
    yearlyData: List<MonthlyProgressData>,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.StackedBarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📅 Yearly Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    items(yearlyData) { data ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .height((data.workoutCount * 10).dp)
                                    .width(20.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(text = data.month)
                        }
                    }
                }
            }
        }
    }
}