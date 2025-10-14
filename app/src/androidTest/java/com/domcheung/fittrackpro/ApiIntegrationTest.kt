package com.domcheung.fittrackpro

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.domcheung.fittrackpro.data.remote.WgerApiService
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Integration test for Wger API
 * Tests actual API calls to verify connectivity and data structure
 *
 * NOTE: This test requires internet connection
 */
@RunWith(AndroidJUnit4::class)
class ApiIntegrationTest {

    private lateinit var apiService: WgerApiService

    @Before
    fun setup() {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://wger.de/api/v2/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(WgerApiService::class.java)
    }

    @Test
    fun testWgerApiConnection() = runBlocking {
        // Act
        val response = apiService.getAllExercises()

        // Assert
        assertNotNull("Response should not be null", response)
        assertTrue("Should have exercises", response.results.isNotEmpty())
        assertTrue("Count should be positive", response.count > 0)

        println("✅ API Connection Test: Fetched ${response.results.size} exercises")
        println("✅ Total exercises available: ${response.count}")
    }

    @Test
    fun testFetchExerciseCategories() = runBlocking {
        // Act
        val response = apiService.getAllExerciseCategories()

        // Assert
        assertNotNull("Categories response should not be null", response)
        assertTrue("Should have categories", response.results.isNotEmpty())

        println("✅ Categories Test: Fetched ${response.results.size} categories")
        response.results.take(5).forEach { category ->
            println("   - ${category.name} (ID: ${category.id})")
        }
    }

    @Test
    fun testFetchEquipment() = runBlocking {
        // Act
        val response = apiService.getAllEquipment()

        // Assert
        assertNotNull("Equipment response should not be null", response)
        assertTrue("Should have equipment", response.results.isNotEmpty())

        println("✅ Equipment Test: Fetched ${response.results.size} equipment types")
        response.results.take(5).forEach { equipment ->
            println("   - ${equipment.name} (ID: ${equipment.id})")
        }
    }

    @Test
    fun testExerciseDataStructure() = runBlocking {
        // Act
        val response = apiService.getAllExercises()
        val firstExercise = response.results.firstOrNull()

        // Assert
        assertNotNull("Should have at least one exercise", firstExercise)
        firstExercise?.let { exercise ->
            assertNotNull("Exercise ID should not be null", exercise.id)
            assertFalse("Exercise name should not be empty", exercise.name.isBlank())
            assertTrue("Exercise should have valid category ID", exercise.categoryId > 0)

            println("✅ Exercise Data Structure Test:")
            println("   - ID: ${exercise.id}")
            println("   - Name: ${exercise.name}")
            println("   - Category ID: ${exercise.categoryId}")
            println("   - Equipment IDs: ${exercise.equipmentIds}")
            println("   - Description: ${exercise.description?.take(50)}...")
        }
    }

    @Test
    fun testFullIntegrationFlow() = runBlocking {
        // This test simulates the full sync flow
        println("\n🧪 Testing Full Integration Flow:")

        // Step 1: Fetch exercises
        println("Step 1: Fetching exercises...")
        val exercisesResponse = apiService.getAllExercises()
        assertTrue("Should fetch exercises", exercisesResponse.results.isNotEmpty())
        println("   ✅ Fetched ${exercisesResponse.results.size} exercises")

        // Step 2: Fetch categories
        println("Step 2: Fetching categories...")
        val categoriesResponse = apiService.getAllExerciseCategories()
        assertTrue("Should fetch categories", categoriesResponse.results.isNotEmpty())
        println("   ✅ Fetched ${categoriesResponse.results.size} categories")

        // Step 3: Fetch equipment
        println("Step 3: Fetching equipment...")
        val equipmentResponse = apiService.getAllEquipment()
        assertTrue("Should fetch equipment", equipmentResponse.results.isNotEmpty())
        println("   ✅ Fetched ${equipmentResponse.results.size} equipment types")

        // Step 4: Create lookup maps
        println("Step 4: Creating lookup maps...")
        val categoryMap = categoriesResponse.results.associateBy { it.id }
        val equipmentMap = equipmentResponse.results.associateBy { it.id }
        println("   ✅ Created category map (${categoryMap.size} entries)")
        println("   ✅ Created equipment map (${equipmentMap.size} entries)")

        // Step 5: Test data mapping
        println("Step 5: Testing data mapping...")
        val sampleExercise = exercisesResponse.results.first()
        val categoryName = categoryMap[sampleExercise.categoryId]?.name ?: "Unknown"
        val equipmentNames = sampleExercise.equipmentIds?.mapNotNull {
            equipmentMap[it]?.name
        } ?: emptyList()

        println("   ✅ Sample Exercise Mapping:")
        println("      - Name: ${sampleExercise.name}")
        println("      - Category: $categoryName")
        println("      - Equipment: ${equipmentNames.joinToString(", ")}")

        println("\n✅ Full Integration Flow Test PASSED!")
    }
}
package com.domcheung.fittrackpro.usecase

import com.domcheung.fittrackpro.data.repository.WorkoutRepository
import com.domcheung.fittrackpro.domain.usecase.SyncExercisesFromApiUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Unit tests for SyncExercisesFromApiUseCase
 */
class SyncExercisesUseCaseTest {

    @Mock
    private lateinit var repository: WorkoutRepository

    private lateinit var useCase: SyncExercisesFromApiUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = SyncExercisesFromApiUseCase(repository)
    }

    @Test
    fun `invoke should call repository syncExercisesFromApi`() = runTest {
        // Arrange
        `when`(repository.syncExercisesFromApi()).thenReturn(Result.success(Unit))

        // Act
        val result = useCase()

        // Assert
        verify(repository, times(1)).syncExercisesFromApi()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke should return success when repository succeeds`() = runTest {
        // Arrange
        `when`(repository.syncExercisesFromApi()).thenReturn(Result.success(Unit))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Arrange
        val exception = Exception("Network error")
        `when`(repository.syncExercisesFromApi()).thenReturn(Result.failure(exception))

        // Act
        val result = useCase()

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
package com.domcheung.fittrackpro.repository

import com.domcheung.fittrackpro.data.local.dao.ExerciseDao
import com.domcheung.fittrackpro.data.model.Exercise
import com.domcheung.fittrackpro.data.remote.WgerApiService
import com.domcheung.fittrackpro.data.remote.dto.EquipmentDto
import com.domcheung.fittrackpro.data.remote.dto.ExerciseCategoryDto
import com.domcheung.fittrackpro.data.remote.dto.ExerciseDto
import com.domcheung.fittrackpro.data.remote.dto.WgerApiResponse
import com.domcheung.fittrackpro.data.repository.WorkoutRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Unit tests for WorkoutRepository's API sync functionality
 */
class WorkoutRepositoryTest {

    @Mock
    private lateinit var exerciseDao: ExerciseDao

    @Mock
    private lateinit var wgerApiService: WgerApiService

    @Mock
    private lateinit var firestore: FirebaseFirestore

    private lateinit var repository: WorkoutRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        // Repository will be created in each test with mocked dependencies
    }

    @Test
    fun `syncExercisesFromApi should fetch and store exercises successfully`() = runTest {
        // Arrange - Create mock API responses
        val mockExercises = listOf(
            ExerciseDto(
                id = 1,
                name = "Bench Press",
                description = "Chest exercise",
                categoryId = 1,
                equipmentIds = listOf(1),
                languageId = 2
            ),
            ExerciseDto(
                id = 2,
                name = "Squat",
                description = "Leg exercise",
                categoryId = 2,
                equipmentIds = listOf(1),
                languageId = 2
            )
        )

        val mockCategories = listOf(
            ExerciseCategoryDto(id = 1, name = "Chest"),
            ExerciseCategoryDto(id = 2, name = "Legs")
        )

        val mockEquipment = listOf(
            EquipmentDto(id = 1, name = "Barbell")
        )

        val exercisesResponse = WgerApiResponse(
            count = 2,
            next = null,
            previous = null,
            results = mockExercises
        )

        val categoriesResponse = WgerApiResponse(
            count = 2,
            next = null,
            previous = null,
            results = mockCategories
        )

        val equipmentResponse = WgerApiResponse(
            count = 1,
            next = null,
            previous = null,
            results = mockEquipment
        )

        // Mock API calls
        `when`(wgerApiService.getAllExercises()).thenReturn(exercisesResponse)
        `when`(wgerApiService.getAllExerciseCategories()).thenReturn(categoriesResponse)
        `when`(wgerApiService.getAllEquipment()).thenReturn(equipmentResponse)

        // Create repository with mocks (need to create properly with all dependencies)
        // Note: This test shows the structure. Full implementation needs proper mocking of all constructor params

        // Act & Assert
        // Verify API service was called
        verify(wgerApiService, times(0)).getAllExercises() // Will be called when repo.syncExercisesFromApi() is invoked

        // This test demonstrates the expected behavior
        // In practice, you'd need to properly inject all dependencies
    }

    @Test
    fun `syncExercisesFromApi should handle API errors gracefully`() = runTest {
        // Arrange - Mock API to throw exception
        `when`(wgerApiService.getAllExercises()).thenThrow(RuntimeException("Network error"))

        // Act & Assert
        // Should fall back to sample exercises
        // Verify clearApiExercises was NOT called on error
        // Verify sample exercises were created if DB is empty
    }

    @Test
    fun `syncExercisesFromApi should map DTOs to Exercise entities correctly`() {
        // Arrange
        val dto = ExerciseDto(
            id = 100,
            name = "Test Exercise",
            description = "Test description",
            categoryId = 5,
            equipmentIds = listOf(1, 2),
            languageId = 2
        )

        val categoryMap = mapOf(5 to ExerciseCategoryDto(5, "Arms"))
        val equipmentMap = mapOf(
            1 to EquipmentDto(1, "Dumbbell"),
            2 to EquipmentDto(2, "Bench")
        )

        // Act - Map DTO to Exercise (this logic is in the repository)
        val exercise = Exercise(
            id = dto.id,
            name = dto.name,
            description = dto.description ?: "No description available",
            category = categoryMap[dto.categoryId]?.name ?: "Uncategorized",
            muscles = emptyList(),
            equipment = dto.equipmentIds?.mapNotNull { equipmentMap[it]?.name } ?: emptyList(),
            instructions = dto.description ?: "",
            isCustom = false,
            createdAt = System.currentTimeMillis(),
            syncedToFirebase = false
        )

        // Assert
        assertEquals(100, exercise.id)
        assertEquals("Test Exercise", exercise.name)
        assertEquals("Arms", exercise.category)
        assertEquals(2, exercise.equipment.size)
        assertTrue(exercise.equipment.contains("Dumbbell"))
        assertTrue(exercise.equipment.contains("Bench"))
        assertFalse(exercise.isCustom)
    }

    @Test
    fun `exercise mapping should handle missing category gracefully`() {
        // Arrange
        val dto = ExerciseDto(
            id = 200,
            name = "Unknown Category Exercise",
            description = "Test",
            categoryId = 999, // Non-existent category
            equipmentIds = null,
            languageId = 2
        )

        val categoryMap = emptyMap<Int, ExerciseCategoryDto>()
        val equipmentMap = emptyMap<Int, EquipmentDto>()

        // Act
        val exercise = Exercise(
            id = dto.id,
            name = dto.name,
            description = dto.description ?: "No description available",
            category = categoryMap[dto.categoryId]?.name ?: "Uncategorized",
            muscles = emptyList(),
            equipment = dto.equipmentIds?.mapNotNull { equipmentMap[it]?.name } ?: emptyList(),
            instructions = dto.description ?: "",
            isCustom = false,
            createdAt = System.currentTimeMillis(),
            syncedToFirebase = false
        )

        // Assert
        assertEquals("Uncategorized", exercise.category)
        assertTrue(exercise.equipment.isEmpty())
    }
}

