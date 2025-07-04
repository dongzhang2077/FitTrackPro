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
import androidx.compose.ui.unit.dp
import com.domcheung.fittrackpro.ui.theme.HandDrawnShapes

// Sample data models
data class PersonalRecord(
    val exercise: String,
    val weight: String,
    val date: String,
    val improvement: String? = null
)

data class WeeklyActivity(
    val day: String,
    val isCompleted: Boolean,
    val dayOfMonth: Int
)

enum class TimeRange(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

@Composable
fun ProgressScreen() {
    var selectedTimeRange by remember { mutableStateOf(TimeRange.WEEK) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            ProgressHeader()
        }

        // Time range selector
        item {
            TimeRangeSelector(
                selectedRange = selectedTimeRange,
                onRangeSelected = { selectedTimeRange = it }
            )
        }

        // Weekly activity chart
        item {
            WeeklyActivityCard()
        }

        // Personal records
        item {
            PersonalRecordsCard()
        }

        // Achievement stats
        item {
            AchievementStatsCard()
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
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.actionCard),
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
            TimeRange.values().forEach { range ->
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
private fun WeeklyActivityCard() {
    val weeklyData = listOf(
        WeeklyActivity("Mon", true, 1),
        WeeklyActivity("Tue", true, 2),
        WeeklyActivity("Wed", false, 3),
        WeeklyActivity("Thu", true, 4),
        WeeklyActivity("Fri", false, 5),
        WeeklyActivity("Sat", false, 6),
        WeeklyActivity("Sun", false, 7)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.progressCard),
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

            // Weekly activity visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyData.forEach { day ->
                    WeeklyActivityItem(
                        day = day.day,
                        isCompleted = day.isCompleted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Completed: 3/3 planned workouts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeeklyActivityItem(
    day: String,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline
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
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = day,
            style = MaterialTheme.typography.bodySmall,
            color = if (isCompleted)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PersonalRecordsCard() {
    val personalRecords = listOf(
        PersonalRecord("Bench Press", "70kg", "3 days ago", "+5kg"),
        PersonalRecord("Squat", "90kg", "1 week ago", "+10kg"),
        PersonalRecord("Deadlift", "100kg", "2 weeks ago", "+15kg")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant1),
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

            personalRecords.forEach { record ->
                PersonalRecordItem(record = record)
                if (record != personalRecords.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun PersonalRecordItem(record: PersonalRecord) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = record.exercise,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = record.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = record.weight,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            record.improvement?.let { improvement ->
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = improvement,
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
private fun AchievementStatsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant2),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AchievementStatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "7",
                    label = "Day Streak",
                    color = MaterialTheme.colorScheme.error
                )

                AchievementStatItem(
                    icon = Icons.Default.EmojiEvents,
                    value = "12",
                    label = "Total PRs",
                    color = MaterialTheme.colorScheme.tertiary
                )

                AchievementStatItem(
                    icon = Icons.Default.Timeline,
                    value = "85%",
                    label = "Goal Rate",
                    color = MaterialTheme.colorScheme.primary
                )
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