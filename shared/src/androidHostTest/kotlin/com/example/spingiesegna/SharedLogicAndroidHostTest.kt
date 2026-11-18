package com.example.spingiesegna

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SharedLogicAndroidHostTest {

    @Test
    fun templateKeepsIndependentRestIntervalsFolderAndMax() {
        val template = WorkoutTemplate(
            id = "template-a",
            name = "Gambe",
            colorId = "green",
            exercises = listOf(
                Exercise("ex-a", "Squat", listOf("MAX", "8-10", "10")),
                Exercise("ex-b", "Affondi", listOf("12"))
            ),
            exerciseRestIntervals = listOf(ExerciseRestInterval("ex-a", "ex-b", 120)),
            setRestIntervals = listOf(SetRestInterval("ex-a", 0, 60), SetRestInterval("ex-a", 1, 135)),
            folderId = "folder-a"
        )

        val decoded = assertNotNull(decodeTemplate(encodeTemplate(template), "blue", 0))

        assertEquals(listOf("MAX", "8-10", "10"), decoded.exercises.first().repetitions)
        assertEquals(template.exerciseRestIntervals, decoded.exerciseRestIntervals)
        assertEquals(template.setRestIntervals, decoded.setRestIntervals)
        assertEquals("folder-a", decoded.folderId)
    }

    @Test
    fun oldTemplateRestValueMigratesToEachSeriesGap() {
        fun fields(vararg values: String) = values.joinToString("") { "${it.length}:$it" }
        val oldV9 = "V9|" + fields(
            "template-old", "Forza", "blue", "60", "1",
            "exercise-old", "Panca", "", "", "", "3",
            "10", "false", "10", "false", "10", "false", "0"
        )

        val decoded = assertNotNull(decodeTemplate(oldV9, "blue", 0))

        assertEquals(listOf(SetRestInterval("exercise-old", 0, 60), SetRestInterval("exercise-old", 1, 60)), decoded.setRestIntervals)
        assertEquals(emptyList(), decoded.exerciseRestIntervals)
    }

    @Test
    fun workoutKeepsRecoveriesPhotoAndMaxAfterRoundTrip() {
        val workout = WorkoutForDate(
            dateKey = "2026-11-18",
            templateId = "template-a",
            templateName = "Gambe",
            colorId = "green",
            exercises = listOf(
                WorkoutExercise("ex-a", "Squat", 2, sets = listOf(WorkoutSet("MAX"), WorkoutSet("8-10"))),
                WorkoutExercise("ex-b", "Affondi", 1, sets = listOf(WorkoutSet("12")))
            ),
            photoFileName = "workout-2026-11-18.jpg",
            exerciseRestIntervals = listOf(ExerciseRestInterval("ex-a", "ex-b", 120)),
            setRestIntervals = listOf(SetRestInterval("ex-a", 0, 75))
        )

        val decoded = assertNotNull(decodeWorkout(encodeWorkout(workout)))

        assertEquals("workout-2026-11-18.jpg", decoded.photoFileName)
        assertEquals(workout.templateId, decoded.templateId)
        assertEquals(workout.exerciseRestIntervals, decoded.exerciseRestIntervals)
        assertEquals(workout.setRestIntervals, decoded.setRestIntervals)
        assertEquals("MAX", decoded.exercises.first().sets.first().plannedRepetitions)
    }

    @Test
    fun oldWorkoutRestValueMigratesWithoutTouchingExerciseRecovery() {
        fun fields(vararg values: String) = values.joinToString("") { "${it.length}:$it" }
        val oldW10 = "W10|" + fields(
            "2026-11-18", "template-old", "Forza", "blue", "45", "", "1",
            "exercise-old", "Panca", "", "2", "false", "false", "false", "", "", "", "2",
            "10", "false", "", "", "10", "false", "", "", "0"
        )

        val decoded = assertNotNull(decodeWorkout(oldW10))

        assertEquals(listOf(SetRestInterval("exercise-old", 0, 45)), decoded.setRestIntervals)
        assertEquals(emptyList(), decoded.exerciseRestIntervals)
    }

    @Test
    fun folderCanBeSavedAndLoaded() {
        val folder = WorkoutFolder("folder-a", "Programma inverno", 12, "2026-11-01", "2027-01-31")

        assertEquals(folder, decodeFolder(encodeFolder(folder)))
    }
}
