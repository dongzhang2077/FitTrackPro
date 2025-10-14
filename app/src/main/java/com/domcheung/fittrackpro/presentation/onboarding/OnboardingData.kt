package com.domcheung.fittrackpro.presentation.onboarding

/**
 * Data class to hold onboarding user information
 */
data class OnboardingData(
    // Basic info - no userName here since it's already in registration
    val selectedAvatar: DefaultAvatar? = null,

    // Physical data
    val height: String = "",
    val currentWeight: String = "",
    val targetWeight: String = "",

    // Fitness goals
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    val primaryGoal: FitnessGoal = FitnessGoal.GENERAL_FITNESS,
    val workoutFrequency: WorkoutFrequency = WorkoutFrequency.THREE_TIMES_PER_WEEK,

    // Progress tracking
    val isCompleted: Boolean = false
)

/**
 * User fitness experience levels
 */
enum class ExperienceLevel(val displayName: String) {
    BEGINNER("Beginner - Just starting out"),
    INTERMEDIATE("Intermediate - Some experience"),
    ADVANCED("Advanced - Experienced lifter")
}

/**
 * Primary fitness goals
 */
enum class FitnessGoal(val displayName: String) {
    GENERAL_FITNESS("General Fitness"),
    MUSCLE_GAIN("Build Muscle"),
    FAT_LOSS("Lose Fat"),
    STRENGTH("Increase Strength")
}

/**
 * Preferred workout frequency
 */
enum class WorkoutFrequency(val displayName: String, val timesPerWeek: Int) {
    ONE_TIME_PER_WEEK("1 time per week", 1),
    TWO_TIMES_PER_WEEK("2 times per week", 2),
    THREE_TIMES_PER_WEEK("3 times per week", 3),
    FOUR_TIMES_PER_WEEK("4 times per week", 4),
    FIVE_TIMES_PER_WEEK("5 times per week", 5),
    SIX_TIMES_PER_WEEK("6 times per week", 6),
    SEVEN_TIMES_PER_WEEK("7 times per week", 7)
}
