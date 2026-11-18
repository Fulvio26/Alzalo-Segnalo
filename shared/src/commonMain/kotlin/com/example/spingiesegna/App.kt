package com.example.spingiesegna

import androidx.compose.foundation.background
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Clock

private val monthNames = listOf(
    "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
    "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
)
private val weekDays = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")
private const val maxFeedbackLength = 120
private data class TemplateColor(val id: String, val label: String, val value: Color)
private val templateColors = listOf(
    TemplateColor("blue", "Blu", Color(0xFF1976D2)),
    TemplateColor("sky", "Celeste", Color(0xFF29B6F6)),
    TemplateColor("yellow", "Giallo", Color(0xFFFBC02D)),
    TemplateColor("green", "Verde", Color(0xFF388E3C)),
    TemplateColor("red", "Rosso scuro", Color(0xFFB71C1C)),
    TemplateColor("magenta", "Magenta", Color(0xFFAD1457)),
    TemplateColor("violet", "Viola", Color(0xFF6A1B9A)),
    TemplateColor("fuchsia", "Fucsia", Color(0xFFE6007E)),
    TemplateColor("pink", "Rosa", Color(0xFFF48FB1)),
    TemplateColor("orange", "Arancione", Color(0xFFEF6C00))
)

private fun findTemplateColor(id: String): TemplateColor =
    templateColors.firstOrNull { it.id == id } ?: templateColors.first()

private val darkAppColors = darkColorScheme(
    primary = Color(0xFFB5E66D), onPrimary = Color(0xFF1C260F),
    primaryContainer = Color(0xFF34451F), onPrimaryContainer = Color(0xFFD4F5A6),
    secondary = Color(0xFFB8C9A0), onSecondary = Color(0xFF25321B),
    background = Color(0xFF101210), onBackground = Color(0xFFE5E8E1),
    surface = Color(0xFF181B18), onSurface = Color(0xFFE5E8E1),
    surfaceVariant = Color(0xFF222722), onSurfaceVariant = Color(0xFFB5BDB0),
    outline = Color(0xFF687064), outlineVariant = Color(0xFF394038),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005)
)
private val lightAppColors = lightColorScheme(
    primary = Color(0xFF42651F), onPrimary = Color.White,
    primaryContainer = Color(0xFFD4F5A6), onPrimaryContainer = Color(0xFF17230B),
    secondary = Color(0xFF526347), onSecondary = Color.White,
    background = Color(0xFFF5F7F2), onBackground = Color(0xFF191D17),
    surface = Color(0xFFFFFFFF), onSurface = Color(0xFF191D17),
    surfaceVariant = Color(0xFFEBEFE7), onSurfaceVariant = Color(0xFF4B5546),
    outline = Color(0xFF788272), outlineVariant = Color(0xFFD5DCCF),
    error = Color(0xFFBA1A1A), onError = Color.White
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(context: Any? = null) {
    var isDarkTheme by remember { mutableStateOf(loadDarkThemePreference() ?: true) }
    var pdfExportError by remember { mutableStateOf(false) }
    var lastDriveBackupTime by remember { mutableStateOf(loadLastDriveBackupTime()) }
    var customExerciseCatalog by remember {
        mutableStateOf(loadCustomExerciseRecords().mapNotNull(::decodeCustomExercise))
    }
    val today = remember { civilDate(Clock.System.now().toEpochMilliseconds() / 86_400_000L) }
    var displayedMonth by remember { mutableIntStateOf(today.year * 12 + today.month - 1) }
    var selectedSection by remember { mutableIntStateOf(0) }
    var selectedDate by remember { mutableStateOf<CivilDate?>(null) }
    var savedTemplates by remember {
        mutableStateOf(loadTemplateRecords().mapIndexedNotNull { index, record ->
            decodeTemplate(record, templateColors[index % templateColors.size].id, index)
        })
    }
    var savedFolders by remember { mutableStateOf(loadWorkoutFolderRecords().mapNotNull(::decodeFolder)) }
    var savedWorkouts by remember {
        mutableStateOf(loadWorkoutRecords().mapNotNull(::decodeWorkout).map { workout ->
                val template = savedTemplates.firstOrNull { it.id == workout.templateId }
                    ?: savedTemplates.firstOrNull { it.name == workout.templateName && it.colorId == workout.colorId }
                    ?: savedTemplates.filter { it.name == workout.templateName }.singleOrNull()
                workout.copy(
                    templateId = template?.id ?: workout.templateId,
                    exercises = workout.exercises.mapIndexed { index, exercise ->
                        if (exercise.isCustom) exercise else {
                            val templateExercise = template?.exercises?.getOrNull(index)?.takeIf { it.name == exercise.name }
                                ?: template?.exercises?.firstOrNull { it.name == exercise.name }
                            if (templateExercise != null) exercise.copy(id = templateExercise.id) else exercise
                        }
                    }
                )
            })
    }
    var isCreatingTemplate by remember { mutableStateOf(false) }
    var editingTemplateIndex by remember { mutableIntStateOf(-1) }
    var pendingDeleteTemplateId by remember { mutableStateOf<String?>(null) }
    var selectedFolderId by remember { mutableStateOf<String?>(null) }
    var editingFolderId by remember { mutableStateOf<String?>(null) }
    var isCreatingFolder by remember { mutableStateOf(false) }
    var pendingDeleteFolderId by remember { mutableStateOf<String?>(null) }
    var folderName by remember { mutableStateOf("") }
    var folderSessionCount by remember { mutableStateOf("4") }
    var folderStartDate by remember { mutableStateOf(dateKey(today)) }
    var folderEndDate by remember { mutableStateOf(dateKey(today)) }
    var templateFolderId by remember { mutableStateOf<String?>(null) }
    var templateName by remember { mutableStateOf("") }
    var templateColorId by remember { mutableStateOf(templateColors.first().id) }
    var templateRestSeconds by remember { mutableIntStateOf(90) }
    var templateExerciseRestSeconds by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var templateSetRestSeconds by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var exerciseDrafts by remember { mutableStateOf(listOf(ExerciseDraft())) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val year = displayedMonth / 12
    val month = displayedMonth % 12 + 1
    val currentRoute = when {
        selectedSection == 0 && selectedDate != null -> AppRoute.Workout(selectedDate!!)
        selectedSection == 0 -> AppRoute.Calendar
        selectedSection == 1 && isCreatingTemplate -> AppRoute.TemplateEditor
        selectedSection == 1 -> AppRoute.Templates
        else -> AppRoute.CloudBackup
    }

    fun updateWorkoutRecord(key: String, update: (WorkoutForDate) -> WorkoutForDate) {
        val index = savedWorkouts.indexOfFirst { it.dateKey == key }
        if (index >= 0) {
            savedWorkouts = savedWorkouts.mapIndexed { currentIndex, workout ->
                if (currentIndex == index) update(workout) else workout
            }
            replaceWorkoutRecords(savedWorkouts.map(::encodeWorkout))
        }
    }

    fun archiveExercises(exercises: List<Exercise>) {
        val updated = customExerciseCatalog.toMutableList()
        exercises.forEach { exercise ->
            val entry = CatalogExercise(
                id = exercise.id,
                familyId = exercise.id,
                nameEn = exercise.name,
                nameIt = exercise.name,
                isCustom = true
            )
            val index = updated.indexOfFirst { it.id == exercise.id }
            if (index >= 0) updated[index] = entry else updated += entry
        }
        customExerciseCatalog = updated
        replaceCustomExerciseRecords(updated.map(::encodeCustomExercise))
    }

    MaterialTheme(colorScheme = if (isDarkTheme) darkAppColors else lightAppColors) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    drawerContentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Text(
                        "Spingi e segna",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 30.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    NavigationDrawerItem(
                        label = { Text("Calendario") },
                        selected = selectedSection == 0,
                        onClick = {
                            selectedSection = 0
                            selectedDate = null
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("Schede") },
                        selected = selectedSection == 1,
                        onClick = {
                            selectedSection = 1
                            selectedDate = null
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("Backup su Drive") },
                        selected = selectedSection == 2,
                        onClick = {
                            selectedSection = 2
                            selectedDate = null
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    NavigationDrawerItem(
                        label = { Text("Tema") },
                        icon = { Text(if (isDarkTheme) "☾" else "☀", fontSize = 20.sp) },
                        badge = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { enabled ->
                                    isDarkTheme = enabled
                                    saveDarkThemePreference(enabled)
                                }
                            )
                        },
                        selected = false,
                        onClick = {
                            isDarkTheme = !isDarkTheme
                            saveDarkThemePreference(isDarkTheme)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopAppBar(
                        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        title = {
                            Text(
                                when {
                                    selectedSection == 1 && isCreatingTemplate -> if (editingTemplateIndex >= 0) "Modifica seduta" else "Nuova seduta"
                                    selectedSection == 1 -> "Schede"
                                    selectedSection == 2 -> "Backup su Drive"
                                    selectedDate != null -> "${selectedDate!!.day} ${monthNames[selectedDate!!.month - 1]} ${selectedDate!!.year}"
                                    else -> "Calendario"
                                }
                            )
                        },
                        navigationIcon = {
                            Text(
                                if (selectedDate != null && selectedSection == 0) "‹" else "☰",
                                modifier = Modifier
                                    .clickable {
                                        if (selectedDate != null && selectedSection == 0) selectedDate = null
                                        else scope.launch { drawerState.open() }
                                    }
                                    .padding(horizontal = 18.dp, vertical = 8.dp),
                                fontSize = 24.sp
                            )
                        }
                    )
                }
            ) { innerPadding ->
                AnimatedContent(
                    targetState = currentRoute,
                    transitionSpec = {
                        when {
                            initialState is AppRoute.Calendar && targetState is AppRoute.Workout ->
                                (scaleIn(tween(340, easing = FastOutSlowInEasing), initialScale = 0.88f) + fadeIn(tween(260))) togetherWith
                                    (scaleOut(tween(300, easing = FastOutLinearInEasing), targetScale = 1.025f) + fadeOut(tween(220)))
                            initialState is AppRoute.Workout && targetState is AppRoute.Calendar ->
                                (scaleIn(tween(300, easing = FastOutSlowInEasing), initialScale = 0.97f) + fadeIn(tween(260))) togetherWith
                                    (scaleOut(tween(340, easing = FastOutLinearInEasing), targetScale = 0.88f) + fadeOut(tween(220)))
                            (initialState is AppRoute.Calendar && targetState is AppRoute.Templates) ||
                                (initialState is AppRoute.Templates && targetState is AppRoute.Calendar) -> {
                                val movingForward = targetState is AppRoute.Templates
                                (slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { if (movingForward) it / 5 else -it / 5 } + fadeIn(tween(220))) togetherWith
                                    (slideOutHorizontally(tween(300, easing = FastOutLinearInEasing)) { if (movingForward) -it / 5 else it / 5 } + fadeOut(tween(200)))
                            }
                            else -> fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                        }
                    },
                    label = "main-screen-transition"
                ) { destination ->
                    when (destination) {
                        is AppRoute.Workout -> {
                            val workoutDate = destination.date
                            val dayWorkout = savedWorkouts.firstOrNull { it.dateKey == dateKey(workoutDate) }
                            WorkoutDayScreen(
                                date = workoutDate,
                                workout = dayWorkout,
                                history = savedWorkouts,
                                templates = savedTemplates,
                                onSharePdf = {
                                    val pdfLines = dayWorkout?.let(::workoutPdfLines).orEmpty()
                                    val platformContext = context
                                    if (dayWorkout != null && platformContext != null) {
                                        if (!shareWorkoutPdf(platformContext, "allenamento-${dayWorkout.dateKey}.pdf",
                                                "Allenamento ${dayWorkout.dateKey}", pdfLines)) {
                                            pdfExportError = true
                                        }
                                    }
                                },
                                onWorkoutPhotoChanged = { photoFileName ->
                                    updateWorkoutRecord(dateKey(workoutDate)) { it.copy(photoFileName = photoFileName) }
                                },
                                onWorkoutPhotoRemoved = {
                                    updateWorkoutRecord(dateKey(workoutDate)) { it.copy(photoFileName = null) }
                                },
                                onSelectTemplate = { template ->
                                    val workout = WorkoutForDate(
                                        dateKey = dateKey(workoutDate),
                                        templateId = template.id,
                                        templateName = template.name,
                                        colorId = template.colorId,
                                        restDurationSeconds = template.restDurationSeconds,
                                        exerciseRestIntervals = template.exerciseRestIntervals,
                                        setRestIntervals = template.setRestIntervals,
                                        exercises = template.exercises.map { exercise ->
                                            WorkoutExercise(
                                                id = exercise.id,
                                                name = exercise.name,
                                                baselineSetCount = exercise.repetitions.size,
                                                sets = exercise.repetitions.mapIndexed { index, repetitions ->
                                                    WorkoutSet(repetitions, isWarmup = index in exercise.warmupSeries)
                                                },
                                                notes = exercise.notes,
                                                groupId = exercise.groupId,
                                                groupType = exercise.groupType
                                            )
                                        }
                                    )
                                    savedWorkouts = savedWorkouts.filterNot { it.dateKey == workout.dateKey } + workout
                                    replaceWorkoutRecords(savedWorkouts.map(::encodeWorkout))
                                },
                                onUpdateExercise = { exerciseId, update ->
                                    updateWorkoutRecord(dateKey(workoutDate)) { current ->
                                        current.copy(exercises = current.exercises.map { exercise ->
                                            if (exercise.id == exerciseId) update(exercise) else exercise
                                        })
                                    }
                                },
                                onSetRestIntervalChanged = { exerciseId, afterSetIndex, seconds ->
                                    updateWorkoutRecord(dateKey(workoutDate)) { current ->
                                        current.copy(setRestIntervals = current.setRestIntervals
                                            .filterNot { it.exerciseId == exerciseId && it.afterSetIndex == afterSetIndex } +
                                            SetRestInterval(exerciseId, afterSetIndex, seconds))
                                    }
                                },
                                onExerciseRestIntervalChanged = { afterExerciseId, beforeExerciseId, seconds ->
                                    updateWorkoutRecord(dateKey(workoutDate)) { current ->
                                        current.copy(exerciseRestIntervals = current.exerciseRestIntervals
                                            .filterNot { it.afterExerciseId == afterExerciseId && it.beforeExerciseId == beforeExerciseId } +
                                            ExerciseRestInterval(afterExerciseId, beforeExerciseId, seconds))
                                    }
                                },
                                onWorkoutSetRemoved = { exerciseId, removedIndex ->
                                    updateWorkoutRecord(dateKey(workoutDate)) { current ->
                                        val remaining = current.setRestIntervals.filterNot {
                                            it.exerciseId == exerciseId && (it.afterSetIndex == removedIndex - 1 || it.afterSetIndex == removedIndex)
                                        }.map { interval ->
                                            if (interval.exerciseId == exerciseId && interval.afterSetIndex > removedIndex) {
                                                interval.copy(afterSetIndex = interval.afterSetIndex - 1)
                                            } else interval
                                        }
                                        current.copy(setRestIntervals = remaining)
                                    }
                                },
                                onAddCustomExercise = { exerciseName ->
                                    val exerciseId = newExerciseId()
                                    archiveExercises(listOf(Exercise(exerciseId, exerciseName.trim(), listOf("10"))))
                                    updateWorkoutRecord(dateKey(workoutDate)) { current ->
                                        current.copy(exercises = current.exercises + WorkoutExercise(
                                            id = exerciseId,
                                            name = exerciseName.trim(),
                                            baselineSetCount = 0,
                                            isCustom = true,
                                            sets = listOf(WorkoutSet("10"))
                                        ))
                                    }
                                },
                                onRemoveWorkout = {
                                    dayWorkout?.photoFileName?.let { photoFileName ->
                                        context?.let { removeWorkoutPhoto(it, photoFileName) }
                                    }
                                    savedWorkouts = savedWorkouts.filterNot { it.dateKey == dateKey(workoutDate) }
                                    replaceWorkoutRecords(savedWorkouts.map(::encodeWorkout))
                                },
                                onGoToTemplates = {
                                    selectedDate = null
                                    selectedSection = 1
                                    selectedFolderId = null
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppRoute.Calendar -> AnimatedContent(
                            targetState = displayedMonth,
                            transitionSpec = {
                                val forward = targetState > initialState
                                (slideInHorizontally(tween(280)) { if (forward) it / 4 else -it / 4 } + fadeIn(tween(220))) togetherWith
                                    (slideOutHorizontally(tween(280)) { if (forward) -it / 4 else it / 4 } + fadeOut(tween(220)))
                            }, label = "month-transition"
                        ) { shownMonth ->
                            val shownYear = shownMonth / 12
                            val monthNumber = shownMonth % 12 + 1
                            CalendarScreen(
                                year = shownYear, month = monthNumber, today = today,
                                onPrevious = { displayedMonth-- }, onNext = { displayedMonth++ },
                                onSelectDay = { selectedDate = CivilDate(shownYear, monthNumber, it) },
                                workouts = savedWorkouts, modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppRoute.Templates -> TemplateList(
                            templates = savedTemplates,
                            folders = savedFolders,
                            selectedFolderId = selectedFolderId,
                            onOpenFolder = { selectedFolderId = it },
                            onBackFromFolder = { selectedFolderId = null },
                            onCreateFolder = {
                                editingFolderId = null
                                folderName = ""
                                folderSessionCount = "4"
                                folderStartDate = dateKey(today)
                                folderEndDate = dateKey(today)
                                isCreatingFolder = true
                            },
                            onEditFolder = { folder ->
                                editingFolderId = folder.id
                                folderName = folder.name
                                folderSessionCount = folder.plannedSessions.toString()
                                folderStartDate = folder.startDate
                                folderEndDate = folder.endDate
                                isCreatingFolder = true
                            },
                            onDeleteFolder = { pendingDeleteFolderId = it.id },
                            onMoveTemplate = { template, folderId ->
                                savedTemplates = savedTemplates.map { existing ->
                                    if (existing.id == template.id) existing.copy(folderId = folderId) else existing
                                }
                                replaceTemplateRecords(savedTemplates.map(::encodeTemplate))
                            },
                            onCreate = {
                                editingTemplateIndex = -1
                                templateName = ""
                                templateRestSeconds = 90
                                templateExerciseRestSeconds = emptyMap()
                                templateSetRestSeconds = emptyMap()
                                templateFolderId = selectedFolderId
                                val unusedColors = templateColors.filter { color ->
                                    savedTemplates.none { it.colorId == color.id }
                                }
                                templateColorId = (unusedColors.ifEmpty { templateColors }).random().id
                                exerciseDrafts = listOf(ExerciseDraft())
                                isCreatingTemplate = true
                            },
                            onEdit = { template ->
                                editingTemplateIndex = savedTemplates.indexOfFirst { it.id == template.id }
                                templateName = template.name
                                templateColorId = template.colorId
                                templateRestSeconds = template.restDurationSeconds
                                templateFolderId = template.folderId
                                templateExerciseRestSeconds = template.exerciseRestIntervals.associate {
                                    exerciseRestIntervalKey(it.afterExerciseId, it.beforeExerciseId) to it.durationSeconds
                                }
                                templateSetRestSeconds = template.setRestIntervals.associate {
                                    setRestIntervalKey(it.exerciseId, it.afterSetIndex) to it.durationSeconds
                                }
                                exerciseDrafts = template.exercises.map { exercise ->
                                    ExerciseDraft(exercise.id, exercise.name, exercise.repetitions, exercise.groupId, exercise.groupType,
                                        exercise.notes, exercise.warmupSeries)
                                }
                                isCreatingTemplate = true
                            },
                            onDelete = { template -> pendingDeleteTemplateId = template.id },
                            modifier = Modifier.padding(innerPadding)
                        )
                        AppRoute.TemplateEditor -> TemplateEditor(
                        name = templateName,
                        onNameChange = { templateName = it },
                        selectedColorId = templateColorId,
                        onColorSelected = { templateColorId = it },
                        exerciseRestSeconds = templateExerciseRestSeconds,
                        onExerciseRestSecondsChanged = { key, seconds ->
                            templateExerciseRestSeconds = templateExerciseRestSeconds + (key to seconds)
                        },
                        setRestSeconds = templateSetRestSeconds,
                        onSetRestSecondsChanged = { key, seconds ->
                            templateSetRestSeconds = templateSetRestSeconds + (key to seconds)
                        },
                        exercises = exerciseDrafts,
                        onExerciseNameChanged = { index, value ->
                            exerciseDrafts[index].apply {
                                name = value
                                nameTouched = true
                            }
                        },
                        onExerciseNotesChanged = { index, value -> exerciseDrafts[index].notes = value },
                        onExerciseNameFocusChanged = { index, focused ->
                            exerciseDrafts[index].apply {
                                if (focused) nameWasFocused = true
                                else if (nameWasFocused) nameTouched = true
                            }
                        },
                        onAddExercise = { exerciseDrafts = exerciseDrafts + ExerciseDraft() },
                        onAddGroupedExercise = { type ->
                            val previous = exerciseDrafts.lastOrNull()
                            if (previous != null && (previous.groupId == null || previous.groupType == type)) {
                                val groupId = previous.groupId ?: newExerciseGroupId()
                                previous.groupId = groupId
                                previous.groupType = type
                                exerciseDrafts = exerciseDrafts + ExerciseDraft(groupId = groupId, groupType = type)
                            }
                        },
                        onRemoveExercise = { index ->
                            val removed = exerciseDrafts.getOrNull(index)
                            val remaining = exerciseDrafts.filterIndexed { i, _ -> i != index }
                            removed?.groupId?.let { groupId ->
                                val remainingGroup = remaining.filter { it.groupId == groupId }
                                if (remainingGroup.size == 1) {
                                    remainingGroup.first().groupId = null
                                    remainingGroup.first().groupType = null
                                }
                            }
                            exerciseDrafts = remaining
                        },
                        onAddSet = { exerciseIndex ->
                            val exercise = exerciseDrafts[exerciseIndex]
                            exercise.sets = exercise.sets + SetDraft()
                        },
                        onRemoveSet = { exerciseIndex, setIndex ->
                            val exercise = exerciseDrafts[exerciseIndex]
                            templateSetRestSeconds = remapSetRestIntervalsAfterRemoval(
                                templateSetRestSeconds, exercise.id, setIndex, exercise.sets.size
                            )
                            exercise.sets = exercise.sets.filterIndexed { i, _ -> i != setIndex }
                        },
                        onCancel = {
                            isCreatingTemplate = false
                            editingTemplateIndex = -1
                        },
                        editorTitle = if (editingTemplateIndex >= 0) "Modifica seduta" else "Nuova seduta",
                        onSave = {
                            val template = WorkoutTemplate(
                                id = if (editingTemplateIndex >= 0) savedTemplates[editingTemplateIndex].id else newTemplateId(),
                                name = templateName.trim(),
                                colorId = templateColorId,
                                restDurationSeconds = templateRestSeconds,
                                exerciseRestIntervals = exerciseDrafts.zipWithNext().map { (before, after) ->
                                    ExerciseRestInterval(
                                        afterExerciseId = before.id,
                                        beforeExerciseId = after.id,
                                        durationSeconds = templateExerciseRestSeconds[
                                            exerciseRestIntervalKey(before.id, after.id)
                                        ] ?: 90
                                    )
                                },
                                setRestIntervals = exerciseDrafts.flatMap { exercise ->
                                    (0 until exercise.sets.lastIndex).map { setIndex ->
                                        val key = setRestIntervalKey(exercise.id, setIndex)
                                        SetRestInterval(exercise.id, setIndex, templateSetRestSeconds[key] ?: 90)
                                    }
                                },
                                folderId = templateFolderId,
                                exercises = exerciseDrafts.map { exercise ->
                                    Exercise(
                                        id = exercise.id,
                                        name = exercise.name.trim(),
                                        repetitions = exercise.sets.map { normalizeRepetitionTarget(it.reps) },
                                        groupId = exercise.groupId,
                                        groupType = exercise.groupType,
                                        notes = exercise.notes.trim().takeIf { it.isNotEmpty() },
                                        warmupSeries = exercise.sets.mapIndexedNotNull { setIndex, set ->
                                            setIndex.takeIf { set.isWarmup }
                                        }.toSet()
                                    )
                                }
                            )
                            archiveExercises(template.exercises)
                            if (editingTemplateIndex >= 0) {
                                savedTemplates = savedTemplates.mapIndexed { index, existing ->
                                    if (index == editingTemplateIndex) template else existing
                                }
                                editingTemplateIndex = -1
                            } else {
                                savedTemplates = savedTemplates + template
                            }
                            replaceTemplateRecords(savedTemplates.map(::encodeTemplate))
                            templateName = ""
                            templateRestSeconds = 90
                            templateExerciseRestSeconds = emptyMap()
                            templateSetRestSeconds = emptyMap()
                            exerciseDrafts = listOf(ExerciseDraft())
                            isCreatingTemplate = false
                        },
                        modifier = Modifier.padding(innerPadding)
                        )
                        AppRoute.CloudBackup -> CloudBackupScreen(
                            context = context,
                            snapshot = AppBackupSnapshot(
                                templateRecords = savedTemplates.map(::encodeTemplate),
                                workoutRecords = savedWorkouts.map(::encodeWorkout),
                                customExerciseRecords = customExerciseCatalog.map(::encodeCustomExercise),
                                darkTheme = isDarkTheme,
                                photoFileNames = savedWorkouts.mapNotNull { it.photoFileName },
                                folderRecords = savedFolders.map(::encodeFolder)
                            ),
                            lastBackupTime = lastDriveBackupTime,
                            onBackupTimeChanged = { time ->
                                saveLastDriveBackupTime(time)
                                lastDriveBackupTime = time
                            },
                            onBackupRestored = { archive ->
                                savedTemplates = archive.templateRecords.mapIndexedNotNull { index, record ->
                                    decodeTemplate(record, templateColors[index % templateColors.size].id, index)
                                }
                                savedWorkouts = archive.workoutRecords.mapNotNull(::decodeWorkout)
                                customExerciseCatalog = archive.customExerciseRecords.mapNotNull(::decodeCustomExercise)
                                savedFolders = archive.folderRecords.mapNotNull(::decodeFolder)
                                selectedFolderId = null
                                isDarkTheme = archive.darkTheme
                                saveDarkThemePreference(archive.darkTheme)
                                isCreatingTemplate = false
                                editingTemplateIndex = -1
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
        if (pendingDeleteTemplateId != null) {
            val templateToDelete = savedTemplates.firstOrNull { it.id == pendingDeleteTemplateId }
            if (templateToDelete != null) {
                AlertDialog(
                    onDismissRequest = { pendingDeleteTemplateId = null },
                    title = { Text("Eliminare la seduta?") },
                    text = { Text("La seduta ‘${templateToDelete.name}’ verrà eliminata dal dispositivo.") },
                    confirmButton = {
                        TextButton(onClick = {
                            savedTemplates = savedTemplates.filterNot { it.id == pendingDeleteTemplateId }
                            replaceTemplateRecords(savedTemplates.map(::encodeTemplate))
                            pendingDeleteTemplateId = null
                        }) { Text("Elimina") }
                    },
                    dismissButton = {
                        TextButton(onClick = { pendingDeleteTemplateId = null }) { Text("Annulla") }
                    }
                )
            }
        }
        if (isCreatingFolder) {
            FolderEditorDialog(
                isEditing = editingFolderId != null,
                name = folderName,
                onNameChanged = { folderName = it },
                plannedSessions = folderSessionCount,
                onPlannedSessionsChanged = { folderSessionCount = it },
                startDate = folderStartDate,
                onStartDateChanged = { folderStartDate = it },
                endDate = folderEndDate,
                onEndDateChanged = { folderEndDate = it },
                onDismiss = { isCreatingFolder = false },
                onSave = {
                    val folder = WorkoutFolder(
                        id = editingFolderId ?: newFolderId(),
                        name = folderName.trim(),
                        plannedSessions = folderSessionCount.toIntOrNull()?.coerceIn(1, 999) ?: 1,
                        startDate = folderStartDate,
                        endDate = folderEndDate
                    )
                    savedFolders = if (editingFolderId == null) savedFolders + folder
                        else savedFolders.map { existing -> if (existing.id == editingFolderId) folder else existing }
                    replaceWorkoutFolderRecords(savedFolders.map(::encodeFolder))
                    editingFolderId = null
                    isCreatingFolder = false
                }
            )
        }
        pendingDeleteFolderId?.let { folderId ->
            val folder = savedFolders.firstOrNull { it.id == folderId }
            if (folder != null) {
                val containedCount = savedTemplates.count { it.folderId == folder.id }
                val containedDescription = when (containedCount) {
                    0 -> "Non ci sono sedute al suo interno."
                    1 -> "La seduta contenuta resterà salvata e sarà spostata tra quelle senza scheda."
                    else -> "Le $containedCount sedute contenute resteranno salvate e saranno spostate tra quelle senza scheda."
                }
                AlertDialog(
                    onDismissRequest = { pendingDeleteFolderId = null },
                    title = { Text("Eliminare la scheda?") },
                    text = {
                        Text("La scheda ‘${folder.name}’ verrà eliminata. $containedDescription")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            savedTemplates = savedTemplates.map { template ->
                                if (template.folderId == folder.id) template.copy(folderId = null) else template
                            }
                            savedFolders = savedFolders.filterNot { it.id == folder.id }
                            replaceTemplateRecords(savedTemplates.map(::encodeTemplate))
                            replaceWorkoutFolderRecords(savedFolders.map(::encodeFolder))
                            if (selectedFolderId == folder.id) selectedFolderId = null
                            pendingDeleteFolderId = null
                        }) { Text("Elimina scheda") }
                    },
                    dismissButton = { TextButton(onClick = { pendingDeleteFolderId = null }) { Text("Annulla") } }
                )
            }
        }
        if (pdfExportError) {
            AlertDialog(
                onDismissRequest = { pdfExportError = false },
                title = { Text("PDF non disponibile") },
                text = { Text("Non è stato possibile creare o condividere il PDF. Riprova tra poco.") },
                confirmButton = { TextButton(onClick = { pdfExportError = false }) { Text("OK") } }
            )
        }
    }
}

@Composable
private fun CloudBackupScreen(
    context: Any?,
    snapshot: AppBackupSnapshot,
    lastBackupTime: Long?,
    onBackupTimeChanged: (Long) -> Unit,
    onBackupRestored: (AppBackupArchive) -> Unit,
    modifier: Modifier = Modifier
) {
    var accessToken by remember { mutableStateOf<String?>(null) }
    var isWorking by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var pendingRestore by remember { mutableStateOf<AppBackupArchive?>(null) }
    val scope = rememberCoroutineScope()
    val launchAuthorization = rememberDriveAuthorizationLauncher(
        onAuthorized = {
            accessToken = it
            statusMessage = "Google Drive collegato per questa sessione."
        },
        onError = { statusMessage = it }
    )

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Backup dei tuoi dati", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Il backup contiene schede, sedute, allenamenti registrati, esercizi personalizzati e foto. Parte solo quando lo richiedi.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (accessToken == null) "Collega il tuo account Google per usare il backup privato dell’app."
                    else "Account Google collegato per questa sessione.",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (accessToken == null) {
                    Button(onClick = launchAuthorization, enabled = !isWorking, modifier = Modifier.fillMaxWidth()) {
                        Text("Collega Google Drive")
                    }
                } else {
                    TextButton(
                        onClick = {
                            val platformContext = context
                            if (platformContext == null) {
                                statusMessage = "Apri questa schermata dall’app Android per scollegare l’account."
                            } else {
                                scope.launch {
                                    isWorking = true
                                    try {
                                        disconnectDriveAuthorization(platformContext)
                                        accessToken = null
                                        statusMessage = "Account Google scollegato. Il backup su Drive non è stato cancellato."
                                    } catch (_: Exception) {
                                        statusMessage = "Non è stato possibile scollegare Google Drive. Riprova."
                                    } finally {
                                        isWorking = false
                                    }
                                }
                            }
                        },
                        enabled = !isWorking
                    ) { Text("Scollega account") }
                }
            }
        }

        Button(
            onClick = {
                val token = accessToken
                val platformContext = context
                if (token == null) statusMessage = "Collega prima Google Drive."
                else if (platformContext == null) statusMessage = "Questa funzione è disponibile nell’app Android."
                else scope.launch {
                    isWorking = true
                    statusMessage = "Sto preparando e caricando il backup…"
                    try {
                        val archive = createAppBackupArchive(platformContext, snapshot)
                        uploadDriveBackup(token, archive)
                        val completedAt = Clock.System.now().toEpochMilliseconds()
                        onBackupTimeChanged(completedAt)
                        statusMessage = "Backup completato e verificato su Google Drive."
                    } catch (error: DriveBackupException) {
                        if (error.requiresAuthorization) accessToken = null
                        statusMessage = error.message ?: "Backup non riuscito. I dati sul telefono sono rimasti invariati."
                    } catch (_: Exception) {
                        statusMessage = "Backup non riuscito. I dati sul telefono sono rimasti invariati."
                    } finally {
                        isWorking = false
                    }
                }
            },
            enabled = accessToken != null && context != null && !isWorking,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Crea/Aggiorna backup su Drive") }

        TextButton(
            onClick = {
                val token = accessToken
                val platformContext = context
                if (token == null) statusMessage = "Collega prima Google Drive."
                else if (platformContext == null) statusMessage = "Questa funzione è disponibile nell’app Android."
                else scope.launch {
                    isWorking = true
                    statusMessage = "Sto scaricando e controllando il backup…"
                    try {
                        val bytes = downloadDriveBackup(token)
                        val archive = readAppBackupArchive(platformContext, bytes)
                        if (archive == null) {
                            statusMessage = "Il file su Drive non è un backup valido o è danneggiato. I dati locali non sono stati modificati."
                        } else {
                            val validationError = validateAppBackupArchive(archive)
                            if (validationError != null) statusMessage = validationError
                            else {
                                pendingRestore = archive
                                statusMessage = null
                            }
                        }
                    } catch (error: DriveBackupException) {
                        if (error.requiresAuthorization) accessToken = null
                        statusMessage = error.message ?: "Non è stato possibile scaricare il backup."
                    } catch (_: Exception) {
                        statusMessage = "Non è stato possibile leggere il backup. I dati locali non sono stati modificati."
                    } finally {
                        isWorking = false
                    }
                }
            },
            enabled = accessToken != null && context != null && !isWorking,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Ripristina da Drive") }

        lastBackupTime?.let { time ->
            Text("Ultimo backup riuscito: ${formatBackupTime(time)}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } ?: Text("Non è ancora stato completato un backup.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (isWorking) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                Text(statusMessage.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            statusMessage?.let { message ->
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
                    Text(message, Modifier.fillMaxWidth().padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    pendingRestore?.let { archive ->
        AlertDialog(
            onDismissRequest = { if (!isWorking) pendingRestore = null },
            title = { Text("Sostituire i dati locali?") },
            text = {
                Text(
                    "Il ripristino sostituirà schede, sedute, allenamenti registrati, esercizi personalizzati, tema e foto presenti su questo telefono. " +
                        "Prima verrà creata una copia locale di sicurezza. Backup del ${formatBackupTime(archive.createdAtMillis)}."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val platformContext = context
                        if (platformContext == null) {
                            statusMessage = "Questa funzione è disponibile nell’app Android."
                            pendingRestore = null
                        } else scope.launch {
                            isWorking = true
                            pendingRestore = null
                            statusMessage = "Sto creando la copia locale e ripristinando…"
                            try {
                                if (!restoreAppBackupArchive(platformContext, archive, snapshot)) {
                                    throw DriveBackupException("Non sono riuscito a completare il ripristino. I dati locali sono rimasti invariati.")
                                }
                                onBackupRestored(archive)
                                statusMessage = "Ripristino completato. I dati e le foto sono stati recuperati."
                            } catch (error: Exception) {
                                statusMessage = error.message ?: "Ripristino non riuscito. I dati locali sono rimasti invariati."
                            } finally {
                                isWorking = false
                            }
                        }
                    },
                    enabled = !isWorking
                ) { Text("Ripristina", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }, enabled = !isWorking) { Text("Annulla") }
            }
        )
    }
}

private fun validateAppBackupArchive(archive: AppBackupArchive): String? {
    if (archive.schemaVersion !in 1..2) return "Questa versione del backup non è supportata. I dati locali non sono stati modificati."
    val templates = archive.templateRecords.mapIndexed { index, record ->
        decodeTemplate(record, templateColors[index % templateColors.size].id, index)
    }
    if (templates.any { it == null }) return "Il backup contiene una seduta incompleta o danneggiata. I dati locali non sono stati modificati."
    val workouts = archive.workoutRecords.map(::decodeWorkout)
    if (workouts.any { it == null }) return "Il backup contiene un allenamento incompleto o danneggiato. I dati locali non sono stati modificati."
    val loadedWorkouts = workouts.filterNotNull()
    if (loadedWorkouts.map { it.dateKey }.distinct().size != loadedWorkouts.size) {
        return "Il backup contiene più allenamenti per lo stesso giorno. I dati locali non sono stati modificati."
    }
    val customExercises = archive.customExerciseRecords.map(::decodeCustomExercise)
    if (customExercises.any { it == null }) return "Il backup contiene un esercizio personalizzato danneggiato. I dati locali non sono stati modificati."
    val folders = archive.folderRecords.map(::decodeFolder)
    if (folders.any { it == null } || folders.filterNotNull().map { it.id }.distinct().size != folders.size) {
        return "Il backup contiene una scheda danneggiata. I dati locali non sono stati modificati."
    }
    val folderIds = folders.filterNotNull().map { it.id }.toSet()
    if (templates.filterNotNull().any { it.folderId != null && it.folderId !in folderIds }) {
        return "Il backup collega una seduta a una scheda mancante. I dati locali non sono stati modificati."
    }
    val photoNames = loadedWorkouts.mapNotNull { it.photoFileName }
    if (photoNames.any { !isSafePhotoFileName(it) } || photoNames.distinct().size != photoNames.size) {
        return "Il backup contiene un collegamento a una foto non valido. I dati locali non sono stati modificati."
    }
    if (photoNames.toSet() != archive.photos.keys) {
        return "Nel backup manca una foto collegata a un allenamento. I dati locali non sono stati modificati."
    }
    if (archive.photos.values.any { it.isEmpty() || it.size > 20 * 1024 * 1024 }) {
        return "Una foto nel backup è vuota o troppo grande. I dati locali non sono stati modificati."
    }
    return null
}

internal fun isSafePhotoFileName(name: String): Boolean =
    name.matches(Regex("[A-Za-z0-9._-]{1,100}")) && !name.contains("..")

private fun datePickerMillis(date: String): Long {
    val parts = date.split('-')
    val year = parts.getOrNull(0)?.toIntOrNull() ?: 1970
    val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
    val day = parts.getOrNull(2)?.toIntOrNull() ?: 1
    val adjustedYear = year - if (month <= 2) 1 else 0
    val era = adjustedYear / 400
    val yearOfEra = adjustedYear - era * 400
    val monthAdjusted = month + if (month > 2) -3 else 9
    val dayOfYear = (153 * monthAdjusted + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    val epochDay = era * 146097L + dayOfEra - 719468
    return epochDay * 86_400_000L
}

private fun dateKeyFromPickerMillis(millis: Long): String = dateKey(civilDate(millis / 86_400_000L))

private fun displayFolderDate(key: String): String {
    val parts = key.split('-')
    val year = parts.getOrNull(0).orEmpty()
    val month = parts.getOrNull(1)?.toIntOrNull()?.let { monthNames.getOrNull(it - 1) }.orEmpty()
    val day = parts.getOrNull(2)?.toIntOrNull()?.toString().orEmpty()
    return "$day $month $year"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderEditorDialog(
    isEditing: Boolean,
    name: String,
    onNameChanged: (String) -> Unit,
    plannedSessions: String,
    onPlannedSessionsChanged: (String) -> Unit,
    startDate: String,
    onStartDateChanged: (String) -> Unit,
    endDate: String,
    onEndDateChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var dateToPick by remember { mutableStateOf<String?>(null) }
    val dateRangeInvalid = endDate < startDate
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Modifica scheda" else "Nuova scheda") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    label = { Text("Nome della scheda") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = plannedSessions,
                    onValueChange = { onPlannedSessionsChanged(it.filter(Char::isDigit).take(3)) },
                    label = { Text("Sedute previste") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                TextButton(onClick = { dateToPick = "inizio" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Inizio: ${displayFolderDate(startDate)}", modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                }
                TextButton(onClick = { dateToPick = "fine" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Fine: ${displayFolderDate(endDate)}", modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                        color = if (dateRangeInvalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
                if (dateRangeInvalid) {
                    Text("La data di fine non può precedere quella di inizio.",
                        color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = name.isNotBlank() && (plannedSessions.toIntOrNull() ?: 0) > 0 && !dateRangeInvalid
            ) { Text("Salva") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } }
    )
    dateToPick?.let { target ->
        val currentDate = if (target == "inizio") startDate else endDate
        val currentPickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = datePickerMillis(currentDate)
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { dateToPick = null },
            confirmButton = {
                TextButton(onClick = {
                    val selected = currentPickerState.selectedDateMillis
                    if (selected != null) {
                        val value = dateKeyFromPickerMillis(selected)
                        if (target == "inizio") onStartDateChanged(value) else onEndDateChanged(value)
                    }
                    dateToPick = null
                }) { Text("Conferma") }
            },
            dismissButton = { TextButton(onClick = { dateToPick = null }) { Text("Annulla") } }
        ) {
            androidx.compose.material3.DatePicker(state = currentPickerState)
        }
    }
}

@Composable
private fun TemplateList(
    templates: List<WorkoutTemplate>,
    folders: List<WorkoutFolder>,
    selectedFolderId: String?,
    onOpenFolder: (String) -> Unit,
    onBackFromFolder: () -> Unit,
    onCreateFolder: () -> Unit,
    onEditFolder: (WorkoutFolder) -> Unit,
    onDeleteFolder: (WorkoutFolder) -> Unit,
    onMoveTemplate: (WorkoutTemplate, String?) -> Unit,
    onCreate: () -> Unit,
    onEdit: (WorkoutTemplate) -> Unit,
    onDelete: (WorkoutTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    var movingTemplateId by remember { mutableStateOf<String?>(null) }
    val currentFolder = folders.firstOrNull { it.id == selectedFolderId }
    val visibleTemplates = templates.filter { it.folderId == selectedFolderId }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (currentFolder != null) {
            TextButton(onClick = onBackFromFolder) { Text("‹  Schede") }
            Text(currentFolder.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${displayFolderDate(currentFolder.startDate)} – ${displayFolderDate(currentFolder.endDate)} · ${currentFolder.plannedSessions} sedute previste",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text("Le tue schede", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Button(onClick = onCreateFolder, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Text("＋  Crea una scheda")
            }
        }
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Text("＋  Crea una seduta")
        }
        if (selectedFolderId == null) {
            Text("Schede", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (folders.isEmpty()) {
                Text("Raggruppa le sedute per programma o periodo.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            folders.forEach { folder ->
                Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(folder.name, modifier = Modifier.fillMaxWidth().clickable { onOpenFolder(folder.id) },
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${displayFolderDate(folder.startDate)} – ${displayFolderDate(folder.endDate)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Text("${folder.plannedSessions} sedute previste",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Row {
                            TextButton(onClick = { onOpenFolder(folder.id) }) { Text("Apri") }
                            TextButton(onClick = { onEditFolder(folder) }) { Text("Modifica") }
                            TextButton(onClick = { onDeleteFolder(folder) }) { Text("Elimina", color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
            Text("Sedute senza scheda", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        if (visibleTemplates.isEmpty()) {
            Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (selectedFolderId == null) "Nessuna seduta senza scheda" else "Nessuna seduta in questa scheda",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Puoi aggiungere una seduta qui oppure spostarne una esistente.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        visibleTemplates.forEach { template ->
            Surface(modifier = Modifier.animateContentSize(tween(280)), color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(modifier = Modifier.size(18.dp), shape = CircleShape,
                            color = findTemplateColor(template.colorId).value) {}
                        Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { onEdit(template) }) { Text("Modifica") }
                        TextButton(onClick = { onDelete(template) }) { Text("Elimina", color = MaterialTheme.colorScheme.error) }
                        Box {
                            TextButton(onClick = { movingTemplateId = template.id }) { Text("Scheda ▾") }
                            DropdownMenu(
                                expanded = movingTemplateId == template.id,
                                onDismissRequest = { movingTemplateId = null },
                                shape = RoundedCornerShape(20.dp),
                                containerColor = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Senza scheda") },
                                    onClick = { onMoveTemplate(template, null); movingTemplateId = null }
                                )
                                folders.forEach { folder ->
                                    DropdownMenuItem(
                                        text = { Text(folder.name) },
                                        onClick = { onMoveTemplate(template, folder.id); movingTemplateId = null }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateEditor(
    name: String,
    onNameChange: (String) -> Unit,
    selectedColorId: String,
    onColorSelected: (String) -> Unit,
    exerciseRestSeconds: Map<String, Int>,
    onExerciseRestSecondsChanged: (String, Int) -> Unit,
    setRestSeconds: Map<String, Int>,
    onSetRestSecondsChanged: (String, Int) -> Unit,
    exercises: List<ExerciseDraft>,
    onExerciseNameChanged: (Int, String) -> Unit,
    onExerciseNotesChanged: (Int, String) -> Unit,
    onExerciseNameFocusChanged: (Int, Boolean) -> Unit,
    onAddExercise: () -> Unit,
    onAddGroupedExercise: (ExerciseGroupType) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit,
    onCancel: () -> Unit,
    editorTitle: String,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var addExerciseMenuExpanded by remember { mutableStateOf(false) }
    var expandedRestInterval by remember { mutableStateOf<String?>(null) }
    val previousExercise = exercises.lastOrNull()
    val canAddSuperset = previousExercise != null &&
        (previousExercise.groupId == null || previousExercise.groupType == ExerciseGroupType.Superset)
    val canAddJumpSet = previousExercise != null &&
        (previousExercise.groupId == null || previousExercise.groupType == ExerciseGroupType.JumpSet)
    val isValid = name.isNotBlank() && exercises.isNotEmpty() && exercises.all {
        it.name.isNotBlank() && it.sets.isNotEmpty() && it.sets.all { set -> isValidRepetitionTarget(set.reps) }
    }
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(editorTitle, style = MaterialTheme.typography.headlineSmall)
            TemplateColorPicker(selectedColorId, onColorSelected)
        }
        OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Nome della seduta") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        exercises.forEachIndexed { index, exercise ->
            Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Esercizio ${index + 1}", style = MaterialTheme.typography.titleSmall)
                        if (exercise.groupId != null && exercise.groupType != null) {
                            val groupMembers = exercises.filter { it.groupId == exercise.groupId }
                            val groupPosition = groupMembers.indexOfFirst { it.id == exercise.id } + 1
                            ExerciseGroupBadge(exercise.groupType!!, groupPosition, groupMembers.size)
                        }
                    }
                    OutlinedTextField(
                        value = exercise.name,
                        onValueChange = { onExerciseNameChanged(index, it) },
                        label = { Text("Nome esercizio *") },
                        isError = exercise.name.isBlank() && exercise.nameTouched,
                        modifier = Modifier.fillMaxWidth().onFocusChanged { onExerciseNameFocusChanged(index, it.isFocused) },
                        singleLine = true
                    )
                    if (exercise.name.isBlank() && exercise.nameTouched) {
                        Text("Il nome dell'esercizio è obbligatorio.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                    OutlinedTextField(
                        value = exercise.notes,
                        onValueChange = { onExerciseNotesChanged(index, it) },
                        label = { Text("Note") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 1,
                        maxLines = 3
                    )
                    exercise.sets.forEachIndexed { setIndex, set ->
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Serie ${setIndex + 1}", style = MaterialTheme.typography.bodyMedium)
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(
                                        modifier = Modifier.clickable { set.isWarmup = !set.isWarmup },
                                        shape = CircleShape,
                                        color = if (set.isWarmup) MaterialTheme.colorScheme.tertiaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            if (set.isWarmup) "Avvicinamento" else "Serie di lavoro",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            color = if (set.isWarmup) MaterialTheme.colorScheme.onTertiaryContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (exercise.sets.size > 1) {
                                        Text("×", modifier = Modifier.clickable { onRemoveSet(index, setIndex) }.padding(8.dp),
                                            color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = set.reps,
                                onValueChange = { set.reps = it.filter { char -> char.isLetterOrDigit() || char == '-' || char == '–' } },
                                label = { Text("Ripetizioni") },
                                modifier = Modifier.fillMaxWidth(), singleLine = true
                            )
                        }
                        if (setIndex < exercise.sets.lastIndex) {
                            val intervalKey = setRestIntervalKey(exercise.id, setIndex)
                            RestIntervalControl(
                                seconds = setRestSeconds[intervalKey] ?: 90,
                                expanded = expandedRestInterval == intervalKey,
                                onToggle = {
                                    expandedRestInterval = if (expandedRestInterval == intervalKey) null else intervalKey
                                },
                                onChange = { onSetRestSecondsChanged(intervalKey, it) }
                            )
                        }
                    }
                    Text("Aggiungi serie", modifier = Modifier.clickable { onAddSet(index) }.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary)
                    if (exercises.size > 1) {
                        Text("Rimuovi esercizio", modifier = Modifier.clickable { onRemoveExercise(index) }.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (index < exercises.lastIndex) {
                val nextExercise = exercises[index + 1]
                val intervalKey = exerciseRestIntervalKey(exercise.id, nextExercise.id)
                val intervalSeconds = exerciseRestSeconds[intervalKey] ?: 90
                RestIntervalControl(
                    seconds = intervalSeconds,
                    expanded = expandedRestInterval == intervalKey,
                    onToggle = {
                        expandedRestInterval = if (expandedRestInterval == intervalKey) null else intervalKey
                    },
                    onChange = { onExerciseRestSecondsChanged(intervalKey, it) }
                )
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onAddExercise, modifier = Modifier.weight(1f)) { Text("Aggiungi esercizio") }
            Box {
                TextButton(
                    onClick = { addExerciseMenuExpanded = true },
                    enabled = exercises.isNotEmpty()
                ) { Text("⋮", fontSize = 26.sp) }
                DropdownMenu(
                    expanded = addExerciseMenuExpanded,
                    onDismissRequest = { addExerciseMenuExpanded = false },
                    modifier = Modifier.widthIn(min = 260.dp),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("Aggiungi in superserie", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                                Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                    Text("S", color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        enabled = canAddSuperset,
                        onClick = {
                            onAddGroupedExercise(ExerciseGroupType.Superset)
                            addExerciseMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Aggiungi in jump set", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                                Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                    Text("J", color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        enabled = canAddJumpSet,
                        onClick = {
                            onAddGroupedExercise(ExerciseGroupType.JumpSet)
                            addExerciseMenuExpanded = false
                        }
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onSave, enabled = isValid, modifier = Modifier.weight(1f)) { Text("Salva seduta") }
            Text("Annulla", modifier = Modifier.clickable(onClick = onCancel).padding(8.dp), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ExerciseGroupBadge(type: ExerciseGroupType, position: Int, total: Int) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape) {
        Text(
            "${type.label} $position/$total",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TemplateColorPicker(selectedColorId: String, onColorSelected: (String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier
                .size(42.dp)
                .semantics { contentDescription = "Scegli il colore della seduta" }
                .clickable { isExpanded = true },
            shape = CircleShape,
            color = findTemplateColor(selectedColorId).value,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface)
        ) {}
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            templateColors.chunked(5).forEach { colorRow ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    colorRow.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .semantics { contentDescription = color.label }
                                .clickable {
                                    onColorSelected(color.id)
                                    isExpanded = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = color.value,
                                border = if (selectedColorId == color.id) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

private class ExerciseDraft(
    id: String = newExerciseId(),
    name: String = "",
    repetitions: List<String> = List(3) { "10" },
    groupId: String? = null,
    groupType: ExerciseGroupType? = null,
    notes: String? = null,
    warmupSeries: Set<Int> = emptySet()
) {
    var id by mutableStateOf(id)
    var name by mutableStateOf(name)
    var nameTouched by mutableStateOf(false)
    var nameWasFocused = false
    var groupId by mutableStateOf(groupId)
    var groupType by mutableStateOf(groupType)
    var notes by mutableStateOf(notes.orEmpty())
    var sets by mutableStateOf(repetitions.mapIndexed { index, reps -> SetDraft(reps, index in warmupSeries) })
}

internal data class WorkoutTemplate(
    val id: String,
    val name: String,
    val colorId: String,
    val exercises: List<Exercise>,
    val restDurationSeconds: Int = 90,
    val exerciseRestIntervals: List<ExerciseRestInterval> = emptyList(),
    val setRestIntervals: List<SetRestInterval> = emptyList(),
    val folderId: String? = null
)
internal data class ExerciseRestInterval(val afterExerciseId: String, val beforeExerciseId: String, val durationSeconds: Int = 90)
internal data class SetRestInterval(val exerciseId: String, val afterSetIndex: Int, val durationSeconds: Int = 90)
internal data class WorkoutFolder(
    val id: String,
    val name: String,
    val plannedSessions: Int,
    val startDate: String,
    val endDate: String
)
private fun exerciseRestIntervalKey(afterExerciseId: String, beforeExerciseId: String): String =
    "$afterExerciseId\u001f$beforeExerciseId"
private fun setRestIntervalKey(exerciseId: String, afterSetIndex: Int): String =
    "set\u001f$exerciseId\u001f$afterSetIndex"
internal data class Exercise(
    val id: String,
    val name: String,
    val repetitions: List<String>,
    val groupId: String? = null,
    val groupType: ExerciseGroupType? = null,
    val notes: String? = null,
    val warmupSeries: Set<Int> = emptySet()
)
internal enum class ExerciseGroupType(val storageId: String, val label: String) {
    Superset("superset", "Superserie"),
    JumpSet("jump-set", "Jump set")
}
private fun exerciseGroupTypeFromStorage(value: String): ExerciseGroupType? =
    ExerciseGroupType.entries.firstOrNull { it.storageId == value }

private class SetDraft(repetitions: String = "10", isWarmup: Boolean = false) {
    var reps by mutableStateOf(repetitions)
    var isWarmup by mutableStateOf(isWarmup)
}

private fun normalizeRepetitionTarget(value: String): String =
    if (value.trim().equals("max", ignoreCase = true)) "MAX" else value.trim().replace('–', '-').replace(" ", "")

private fun isValidRepetitionTarget(value: String): Boolean {
    val normalized = normalizeRepetitionTarget(value)
    if (normalized == "MAX") return true
    val pieces = normalized.split('-')
    if (pieces.size == 1) return (pieces[0].toIntOrNull() ?: 0) > 0
    if (pieces.size != 2) return false
    val minimum = pieces[0].toIntOrNull() ?: return false
    val maximum = pieces[1].toIntOrNull() ?: return false
    return minimum > 0 && maximum >= minimum
}

private fun displayRepetitionTarget(value: String): String =
    if (value.equals("max", ignoreCase = true)) "MAX" else value.replace('-', '–')

// Il formato a lunghezza prefissata conserva anche nomi con simboli o separatori.
private fun newTemplateId(): String = "template-${Clock.System.now().toEpochMilliseconds()}-${Random.nextLong().toString(16)}"
private fun newFolderId(): String = "folder-${Clock.System.now().toEpochMilliseconds()}-${Random.nextLong().toString(16)}"
private fun newExerciseId(): String = "exercise-${Clock.System.now().toEpochMilliseconds()}-${Random.nextLong().toString(16)}"
private fun newExerciseGroupId(): String = "exercise-group-${Clock.System.now().toEpochMilliseconds()}-${Random.nextLong().toString(16)}"

internal fun encodeFolder(folder: WorkoutFolder): String = buildString {
    fun addField(value: String) { append(value.length).append(':').append(value) }
    append("F1|")
    addField(folder.id)
    addField(folder.name)
    addField(folder.plannedSessions.toString())
    addField(folder.startDate)
    addField(folder.endDate)
}

internal fun decodeFolder(record: String): WorkoutFolder? = runCatching {
    require(record.startsWith("F1|"))
    val data = record.removePrefix("F1|")
    var position = 0
    fun readField(): String {
        val separator = data.indexOf(':', position)
        require(separator >= position)
        val length = data.substring(position, separator).toInt()
        val start = separator + 1
        val end = start + length
        require(end <= data.length)
        position = end
        return data.substring(start, end)
    }
    val folder = WorkoutFolder(
        id = readField(),
        name = readField(),
        plannedSessions = readField().toInt().coerceIn(1, 999),
        startDate = readField(),
        endDate = readField()
    )
    require(position == data.length && folder.id.isNotBlank() && folder.name.isNotBlank())
    require(folder.startDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    require(folder.endDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) && folder.endDate >= folder.startDate)
    folder
}.getOrNull()

internal fun encodeTemplate(template: WorkoutTemplate): String = buildString {
    fun addField(value: String) { append(value.length).append(':').append(value) }
    append("V10|")
    addField(template.id)
    addField(template.name)
    addField(template.colorId)
    addField(template.restDurationSeconds.toString())
    addField(template.exercises.size.toString())
    template.exercises.forEach { exercise ->
        addField(exercise.id)
        addField(exercise.name)
        addField(exercise.notes.orEmpty())
        addField(exercise.groupId.orEmpty())
        addField(exercise.groupType?.storageId.orEmpty())
        addField(exercise.repetitions.size.toString())
        exercise.repetitions.forEachIndexed { index, repetitions ->
            addField(repetitions)
            addField((index in exercise.warmupSeries).toString())
        }
    }
    addField(template.exerciseRestIntervals.size.toString())
    template.exerciseRestIntervals.forEach { interval ->
        addField(interval.afterExerciseId)
        addField(interval.beforeExerciseId)
        addField(interval.durationSeconds.toString())
    }
    addField(template.setRestIntervals.size.toString())
    template.setRestIntervals.forEach { interval ->
        addField(interval.exerciseId)
        addField(interval.afterSetIndex.toString())
        addField(interval.durationSeconds.toString())
    }
    addField(template.folderId.orEmpty())
}

internal fun decodeTemplate(record: String, fallbackColorId: String, legacyIndex: Int): WorkoutTemplate? = runCatching {
    val isFormat10 = record.startsWith("V10|")
    val isFormat9 = record.startsWith("V9|")
    val isFormat8 = record.startsWith("V8|")
    val isFormat7 = record.startsWith("V7|")
    val isFormat6 = record.startsWith("V6|")
    val isFormat5 = record.startsWith("V5|")
    val isFormat4 = record.startsWith("V4|")
    val isFormat3 = record.startsWith("V3|")
    val isFormat2 = record.startsWith("V2|")
    val hasColor = isFormat3 || isFormat4 || isFormat5 || isFormat6 || isFormat7 || isFormat8 || isFormat9 || isFormat10
    val hasExerciseIds = isFormat5 || isFormat6 || isFormat7 || isFormat8 || isFormat9 || isFormat10
    val hasIndividualRepetitions = isFormat2 || isFormat3 || isFormat4 || isFormat5 || isFormat6 || isFormat7 || isFormat8 || isFormat9 || isFormat10
    val data = when {
        isFormat10 -> record.removePrefix("V10|")
        isFormat9 -> record.removePrefix("V9|")
        isFormat8 -> record.removePrefix("V8|")
        isFormat7 -> record.removePrefix("V7|")
        isFormat6 -> record.removePrefix("V6|")
        isFormat5 -> record.removePrefix("V5|")
        isFormat4 -> record.removePrefix("V4|")
        isFormat3 -> record.removePrefix("V3|")
        isFormat2 -> record.removePrefix("V2|")
        else -> record
    }
    var position = 0
    fun readField(): String {
        val separator = data.indexOf(':', position)
        require(separator >= position)
        val length = data.substring(position, separator).toInt()
        val start = separator + 1
        val end = start + length
        require(end <= data.length)
        position = end
        return data.substring(start, end)
    }
    val id = if (isFormat4 || isFormat5 || isFormat6 || isFormat7 || isFormat8 || isFormat9 || isFormat10) readField() else "template-migrated-$legacyIndex"
    val name = readField()
    val colorId = if (hasColor) readField() else fallbackColorId
    val restDurationSeconds = if (isFormat8 || isFormat9 || isFormat10) readField().toInt().coerceIn(15, 600) else 90
    val count = readField().toInt()
    require(count in 1..100)
    val exercises = List(count) { exerciseIndex ->
        val exerciseId = if (hasExerciseIds) readField() else "$id-exercise-$exerciseIndex"
        val exerciseName = readField()
        val notes = if (isFormat7 || isFormat8 || isFormat9 || isFormat10) readField().takeIf { it.isNotEmpty() } else null
        val groupId = if (isFormat6 || isFormat7 || isFormat8 || isFormat9 || isFormat10) readField().takeIf { it.isNotEmpty() } else null
        val groupType = if (isFormat6 || isFormat7 || isFormat8 || isFormat9 || isFormat10) exerciseGroupTypeFromStorage(readField()) else null
        if (hasIndividualRepetitions) {
            val setCount = readField().toInt()
            require(setCount in 1..100)
            val repetitions = mutableListOf<String>()
            val warmupSeries = mutableSetOf<Int>()
            repeat(setCount) { setIndex ->
                repetitions += readField()
                if ((isFormat7 || isFormat8 || isFormat9 || isFormat10) && readField().toBooleanStrict()) warmupSeries += setIndex
            }
            Exercise(exerciseId, exerciseName, repetitions, groupId, groupType, notes, warmupSeries)
        } else {
            val setCount = readField().toInt()
            val repetitions = readField()
            require(setCount in 1..100)
            Exercise(exerciseId, exerciseName, List(setCount) { repetitions }, groupId, groupType)
        }
    }
    val restIntervals = if (isFormat9 || isFormat10) {
        val intervalCount = readField().toInt()
        require(intervalCount in 0..99)
        List(intervalCount) {
            ExerciseRestInterval(readField(), readField(), readField().toInt().coerceIn(15, 600))
        }
    } else emptyList()
    val setRestIntervals = if (isFormat10) {
        val intervalCount = readField().toInt()
        require(intervalCount in 0..10_000)
        List(intervalCount) {
            SetRestInterval(readField(), readField().toInt().coerceAtLeast(0), readField().toInt().coerceIn(15, 600))
        }
    } else if (restDurationSeconds != 90) {
        exercises.flatMap { exercise ->
            (0 until exercise.repetitions.lastIndex).map { index -> SetRestInterval(exercise.id, index, restDurationSeconds) }
        }
    } else emptyList()
    val folderId = if (isFormat10) readField().takeIf { it.isNotEmpty() } else null
    require(position == data.length)
    WorkoutTemplate(id, name, colorId, exercises, restDurationSeconds, restIntervals, setRestIntervals, folderId)
}.getOrNull()

@Composable
private fun CalendarScreen(
    year: Int,
    month: Int,
    today: CivilDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelectDay: (Int) -> Unit,
    workouts: List<WorkoutForDate>,
    modifier: Modifier = Modifier
) {
    val firstWeekday = weekdayMondayFirst(year, month)
    val daysInMonth = daysInMonth(year, month)
    val monthWorkouts = workouts
        .filter { it.dateKey.startsWith("${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-") }
        .distinctBy { it.templateName to it.colorId }
    val monthWorkoutDays = workouts
        .filter { it.dateKey.startsWith("${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-") }
        .sortedBy { it.dateKey }
    val weekCount = (firstWeekday + daysInMonth + 6) / 7
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", modifier = Modifier.size(48.dp).clickable(onClick = onPrevious).padding(4.dp), fontSize = 36.sp, color = MaterialTheme.colorScheme.primary)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${monthNames[month - 1]} $year", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    if (monthWorkoutDays.size == 1) "1 allenamento questo mese" else "${monthWorkoutDays.size} allenamenti questo mese",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("›", modifier = Modifier.size(48.dp).clickable(onClick = onNext).padding(4.dp), fontSize = 36.sp, color = MaterialTheme.colorScheme.primary)
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            weekDays.forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        repeat(weekCount) { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(7) { weekday ->
                    val day = week * 7 + weekday - firstWeekday + 1
                    val isInMonth = day in 1..daysInMonth
                    val isToday = isInMonth && today.year == year && today.month == month && today.day == day
                    val dayWorkout = if (isInMonth) workouts.firstOrNull {
                        it.dateKey == dateKey(CivilDate(year, month, day))
                    } else null
                    if (isInMonth) {
                        val cellColor = when {
                            dayWorkout != null -> findTemplateColor(dayWorkout.colorId).value
                            isToday -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val textColor = when {
                            dayWorkout != null -> foregroundForColor(dayWorkout.colorId)
                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        Surface(
                            modifier = Modifier.weight(1f).height(82.dp).clickable { onSelectDay(day) },
                            shape = MaterialTheme.shapes.medium,
                            color = cellColor,
                            border = if (isToday) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(horizontal = 3.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                Text(day.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                                if (dayWorkout != null) {
                                    Text(
                                        dayWorkout.templateName,
                                        fontSize = 9.sp,
                                        lineHeight = 11.sp,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(Modifier.weight(1f).height(82.dp))
                    }
                }
            }
            Spacer(Modifier.height(5.dp))
        }
        if (monthWorkouts.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Legenda colori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            monthWorkouts.chunked(2).forEach { rowWorkouts ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowWorkouts.forEach { workout ->
                        Row(
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(Modifier.size(12.dp), shape = CircleShape, color = findTemplateColor(workout.colorId).value) {}
                            Text(workout.templateName, maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    repeat(2 - rowWorkouts.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        if (monthWorkoutDays.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text("Allenamenti del mese", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            monthWorkoutDays.forEach { workout ->
                val workoutDay = workout.dateKey.takeLast(2).toIntOrNull()
                if (workoutDay != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            .clickable { onSelectDay(workoutDay) },
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(Modifier.size(14.dp), shape = CircleShape, color = findTemplateColor(workout.colorId).value) {}
                            Text(workoutDay.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(32.dp))
                            Text("${monthNames[month - 1]} · ${workout.templateName}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

private fun foregroundForColor(colorId: String): Color =
    if (colorId in setOf("yellow", "sky", "pink")) Color.Black else Color.White

@Composable
private fun WorkoutDayScreen(
    date: CivilDate,
    workout: WorkoutForDate?,
    history: List<WorkoutForDate>,
    templates: List<WorkoutTemplate>,
    onSharePdf: () -> Unit,
    onWorkoutPhotoChanged: (String) -> Unit,
    onWorkoutPhotoRemoved: () -> Unit,
    onSelectTemplate: (WorkoutTemplate) -> Unit,
    onUpdateExercise: (String, (WorkoutExercise) -> WorkoutExercise) -> Unit,
    onSetRestIntervalChanged: (String, Int, Int) -> Unit,
    onExerciseRestIntervalChanged: (String, String, Int) -> Unit,
    onWorkoutSetRemoved: (String, Int) -> Unit,
    onAddCustomExercise: (String) -> Unit,
    onRemoveWorkout: () -> Unit,
    onGoToTemplates: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isChoosingTemplate by remember { mutableStateOf(false) }
    var isConfirmingRemoval by remember { mutableStateOf(false) }
    var isAddingExercise by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }
    var feedbackExerciseId by remember { mutableStateOf<String?>(null) }
    var feedbackDraft by remember { mutableStateOf("") }
    var restSeconds by remember { mutableIntStateOf(0) }
    var activeRestKey by remember(workout?.dateKey) { mutableStateOf<String?>(null) }
    var activeRestLabel by remember(workout?.dateKey) { mutableStateOf("") }
    var expandedRestInterval by remember(workout?.dateKey) { mutableStateOf<String?>(null) }
    var expandedExerciseIds by remember(workout?.dateKey) { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(restSeconds > 0) {
        while (restSeconds > 0) {
            delay(1_000)
            restSeconds = (restSeconds - 1).coerceAtLeast(0)
        }
        activeRestKey = null
        activeRestLabel = ""
    }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Allenamento dell'${date.day} ${monthNames[date.month - 1]} ${date.year}",
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (restSeconds > 0) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("◷  $activeRestLabel · ${formatTimer(restSeconds)}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { restSeconds = 0 }) {
                        Text("Ferma", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        if (workout == null) {
            Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Giornata libera", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("Nessun allenamento registrato per questo giorno.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (templates.isEmpty()) {
                Text("Crea prima una seduta per poterla aggiungere a questo giorno.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onGoToTemplates) { Text("Vai alle schede") }
            } else {
                Button(onClick = { isChoosingTemplate = true }) { Text("Aggiungi seduta") }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(Modifier.size(20.dp), shape = CircleShape, color = findTemplateColor(workout.colorId).value) {}
                Text(workout.templateName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            val visibleWorkoutExercises = workout.exercises.filterNot { it.isRemoved }
            visibleWorkoutExercises.forEachIndexed { visibleIndex, exercise ->
                val isExpanded = exercise.id in expandedExerciseIds
                val previousNote = previousExerciseNote(workout, exercise, history)
                val previousFeedback = previousExerciseFeedback(workout, exercise, history)
                val toggleExpansion = {
                    expandedExerciseIds = if (isExpanded) expandedExerciseIds - exercise.id else expandedExerciseIds + exercise.id
                }
                Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f).clickable(onClick = toggleExpansion), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Esercizio ${visibleIndex + 1}", style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (exercise.groupId != null && exercise.groupType != null) {
                                        val groupMembers = workout.exercises.filter { it.groupId == exercise.groupId }
                                        val groupPosition = groupMembers.indexOfFirst { it.id == exercise.id } + 1
                                        ExerciseGroupBadge(exercise.groupType!!, groupPosition, groupMembers.size)
                                    }
                                }
                                Text(exercise.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                                val status = when {
                                    exercise.isSkipped -> "Saltato oggi"
                                    previousNote != null -> previousNote
                                    exercise.isCustom -> "Aggiunto solo per oggi"
                                    else -> null
                                }
                                if (status != null) {
                                    Text(status, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (previousFeedback != null) {
                                FeedbackInfoButton(previousFeedback)
                            }
                            Text(if (isExpanded) "⌃" else "⌄", modifier = Modifier.clickable(onClick = toggleExpansion), fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(tween(220, easing = FastOutSlowInEasing)) + fadeIn(tween(150)),
                            exit = shrinkVertically(tween(180, easing = FastOutLinearInEasing)) + fadeOut(tween(120))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                exercise.notes?.takeIf { it.isNotBlank() }?.let { note ->
                                    Surface(color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.medium) {
                                        Column(Modifier.fillMaxWidth().padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            Text("Note", style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(note, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                                if (exercise.isSkipped) {
                                    Text("Questo esercizio è segnato come saltato.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    TextButton(onClick = {
                                        onUpdateExercise(exercise.id) { it.copy(isSkipped = false, isRemoved = false) }
                                    }) { Text("Riprendi esercizio") }
                                } else {
                                    WorkoutSetTable(
                                        exerciseId = exercise.id,
                                        sets = exercise.sets,
                                        hints = previousSetHints(workout, exercise, history),
                                        setRestIntervals = workout.setRestIntervals,
                                        expandedRestIntervalKey = expandedRestInterval,
                                        activeRestKey = activeRestKey,
                                        onToggleRestInterval = { key ->
                                            expandedRestInterval = if (expandedRestInterval == key) null else key
                                        },
                                        onSetRestIntervalChanged = { setIndex, seconds ->
                                            onSetRestIntervalChanged(exercise.id, setIndex, seconds)
                                        },
                                        onStartRestTimer = { key, seconds, label ->
                                            activeRestKey = key
                                            activeRestLabel = label
                                            restSeconds = seconds
                                        },
                                        onStopRestTimer = { restSeconds = 0 },
                                        onSetChanged = { setIndex, load, completedRepetitions ->
                                            onUpdateExercise(exercise.id) { current ->
                                                current.copy(sets = current.sets.mapIndexed { index, set ->
                                                    if (index == setIndex) set.copy(load = load, completedRepetitions = completedRepetitions) else set
                                                })
                                            }
                                        },
                                        onRemoveSet = { setIndex ->
                                            onWorkoutSetRemoved(exercise.id, setIndex)
                                            onUpdateExercise(exercise.id) { current ->
                                                if (current.sets.size <= 1) current else current.copy(
                                                    sets = current.sets.filterIndexed { index, _ -> index != setIndex }
                                                )
                                            }
                                        },
                                        onSetTypeChanged = { setIndex ->
                                            onUpdateExercise(exercise.id) { current ->
                                                current.copy(sets = current.sets.mapIndexed { index, set ->
                                                    if (index == setIndex) set.copy(isWarmup = !set.isWarmup) else set
                                                })
                                            }
                                        }
                                    )
                                    TextButton(onClick = {
                                        onUpdateExercise(exercise.id) { current ->
                                            val lastTarget = current.sets.lastOrNull()?.plannedRepetitions ?: "10"
                                            current.copy(sets = current.sets + WorkoutSet(lastTarget))
                                        }
                                    }) { Text("+ Aggiungi serie") }
                                    TextButton(onClick = {
                                        onUpdateExercise(exercise.id) { it.copy(isSkipped = true) }
                                    }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Salta esercizio", modifier = Modifier.fillMaxWidth(),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                                    }
                                }
                            }
                        }
                        if (isExpanded) {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(1.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            ) {}
                            TextButton(
                                onClick = {
                                    feedbackExerciseId = exercise.id
                                    feedbackDraft = exercise.feedback.orEmpty().take(maxFeedbackLength)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (exercise.feedback.isNullOrBlank()) "Aggiungi feedback" else "Modifica feedback",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                                )
                            }
                            TextButton(
                                onClick = {
                                    onUpdateExercise(exercise.id) { it.copy(isRemoved = true, isSkipped = true) }
                                    expandedExerciseIds = expandedExerciseIds - exercise.id
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Rimuovi l'esercizio da oggi",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                if (visibleIndex < visibleWorkoutExercises.lastIndex) {
                    val nextExercise = visibleWorkoutExercises[visibleIndex + 1]
                    val interval = workout.exerciseRestIntervals.firstOrNull {
                        it.afterExerciseId == exercise.id && it.beforeExerciseId == nextExercise.id
                    }
                    val intervalSeconds = interval?.durationSeconds ?: 90
                    val intervalKey = exerciseRestIntervalKey(exercise.id, nextExercise.id)
                    RestIntervalControl(
                        seconds = intervalSeconds,
                        expanded = expandedRestInterval == intervalKey,
                        onToggle = {
                            expandedRestInterval = if (expandedRestInterval == intervalKey) null else intervalKey
                        },
                        onChange = { onExerciseRestIntervalChanged(exercise.id, nextExercise.id, it) },
                        onStart = {
                            activeRestKey = intervalKey
                            activeRestLabel = "tra esercizi"
                            restSeconds = intervalSeconds
                        },
                        isRunning = activeRestKey == intervalKey && restSeconds > 0,
                        onStop = { restSeconds = 0 }
                    )
                }
            }
            TextButton(onClick = { isAddingExercise = true }) { Text("+ Aggiungi esercizio") }
            TextButton(onClick = { isConfirmingRemoval = true }) {
                Text("Rimuovi seduta da questo giorno", color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = onSharePdf, modifier = Modifier.fillMaxWidth()) { Text("Condividi riepilogo PDF") }
            WorkoutPhotoAttachment(
                dateKey = workout.dateKey,
                photoFileName = workout.photoFileName,
                onPhotoChanged = onWorkoutPhotoChanged,
                onPhotoRemoved = onWorkoutPhotoRemoved
            )
        }
    }
    if (isChoosingTemplate) {
        AlertDialog(
            onDismissRequest = { isChoosingTemplate = false },
            title = { Text("Scegli una seduta") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    templates.forEach { template ->
                        TextButton(
                            onClick = {
                                onSelectTemplate(template)
                                isChoosingTemplate = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(Modifier.size(18.dp), shape = CircleShape, color = findTemplateColor(template.colorId).value) {}
                                Text(template.name)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { isChoosingTemplate = false }) { Text("Annulla") } }
        )
    }
    if (isConfirmingRemoval) {
        AlertDialog(
            onDismissRequest = { isConfirmingRemoval = false },
            title = { Text("Rimuovere la seduta?") },
            text = { Text("La seduta verrà rimossa da questo giorno. La seduta originale e gli altri allenamenti resteranno invariati.") },
            confirmButton = {
                TextButton(onClick = {
                    onRemoveWorkout()
                    isConfirmingRemoval = false
                }) { Text("Rimuovi", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { isConfirmingRemoval = false }) { Text("Annulla") }
            }
        )
    }
    if (isAddingExercise && workout != null) {
        val removedExercises = workout.exercises.filter { it.isRemoved }
        AlertDialog(
            onDismissRequest = { isAddingExercise = false },
            title = { Text("Aggiungi esercizio") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (removedExercises.isNotEmpty()) {
                        Text("Ripristina un esercizio della seduta", style = MaterialTheme.typography.labelLarge)
                        removedExercises.forEach { exercise ->
                            TextButton(
                                onClick = {
                                    onUpdateExercise(exercise.id) { it.copy(isRemoved = false, isSkipped = false) }
                                    isAddingExercise = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(exercise.name) }
                        }
                    }
                    OutlinedTextField(
                        value = newExerciseName,
                        onValueChange = { newExerciseName = it },
                        label = { Text("Nome dell'esercizio") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = newExerciseName.isNotBlank(),
                    onClick = {
                        onAddCustomExercise(newExerciseName.trim())
                        newExerciseName = ""
                        isAddingExercise = false
                    }
                ) { Text("Aggiungi") }
            },
            dismissButton = { TextButton(onClick = { isAddingExercise = false }) { Text("Annulla") } }
        )
    }
    if (feedbackExerciseId != null && workout != null) {
        val exerciseBeingEdited = workout.exercises.firstOrNull { it.id == feedbackExerciseId }
        if (exerciseBeingEdited != null) {
            AlertDialog(
                onDismissRequest = { feedbackExerciseId = null },
                title = { Text(if (exerciseBeingEdited.feedback.isNullOrBlank()) "Aggiungi feedback" else "Modifica feedback") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = feedbackDraft,
                            onValueChange = { feedbackDraft = it.take(maxFeedbackLength) },
                            label = { Text("Consiglio per la prossima volta") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                        Text(
                            "${feedbackDraft.length}/$maxFeedbackLength",
                            modifier = Modifier.align(Alignment.End),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!exerciseBeingEdited.feedback.isNullOrBlank()) {
                            TextButton(onClick = {
                                onUpdateExercise(exerciseBeingEdited.id) { it.copy(feedback = null) }
                                feedbackExerciseId = null
                            }) { Text("Rimuovi feedback", color = MaterialTheme.colorScheme.error) }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = feedbackDraft.isNotBlank(),
                        onClick = {
                            onUpdateExercise(exerciseBeingEdited.id) { it.copy(feedback = feedbackDraft.trim()) }
                            feedbackExerciseId = null
                        }
                    ) { Text("Salva") }
                },
                dismissButton = { TextButton(onClick = { feedbackExerciseId = null }) { Text("Annulla") } }
            )
        }
    }
}

@Composable
private fun FeedbackInfoButton(feedback: String) {
    var isOpen by remember(feedback) { mutableStateOf(false) }
    Box {
        Text(
            "ⓘ",
            modifier = Modifier.clickable { isOpen = !isOpen }.padding(horizontal = 8.dp),
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )
        if (isOpen) {
            Popup(
                alignment = Alignment.TopEnd,
                onDismissRequest = { isOpen = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    modifier = Modifier.widthIn(max = 260.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(feedback.take(maxFeedbackLength), modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun WorkoutSetTable(
    exerciseId: String,
    sets: List<WorkoutSet>,
    hints: List<WorkoutSetHint>,
    setRestIntervals: List<SetRestInterval>,
    expandedRestIntervalKey: String?,
    activeRestKey: String?,
    onToggleRestInterval: (String) -> Unit,
    onSetRestIntervalChanged: (Int, Int) -> Unit,
    onStartRestTimer: (String, Int, String) -> Unit,
    onStopRestTimer: () -> Unit,
    onSetChanged: (Int, String, String) -> Unit,
    onRemoveSet: (Int) -> Unit,
    onSetTypeChanged: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sets.forEachIndexed { index, set ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SERIE ${index + 1}", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Surface(
                            modifier = Modifier.clickable { onSetTypeChanged(index) },
                            color = if (set.isWarmup) MaterialTheme.colorScheme.tertiaryContainer
                                else MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                if (set.isWarmup) "Avvicinamento" else "Serie di lavoro",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = if (set.isWarmup) MaterialTheme.colorScheme.onTertiaryContainer
                                    else MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) {
                            Text("${displayRepetitionTarget(set.plannedRepetitions)} ripetizioni",
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                        if (sets.size > 1) TextButton(onClick = { onRemoveSet(index) }) { Text("Rimuovi serie") }
                    }
                    val previous = hints.getOrNull(index)
                    Text(
                        "Ultima volta  ·  ${previous?.load?.takeIf { it.isNotBlank() }?.let { "$it kg" } ?: "—"}  ·  ${previous?.completedRepetitions?.takeIf { it.isNotBlank() } ?: "—"} rip.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = set.load,
                            onValueChange = { value -> onSetChanged(index, value.filter { it.isDigit() || it == '.' || it == ',' }, set.completedRepetitions) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Carico (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = set.completedRepetitions,
                            onValueChange = { value -> onSetChanged(index, set.load, value.filter(Char::isDigit)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Ripetizioni effettuate") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }
            }
            if (index < sets.lastIndex) {
                val intervalKey = setRestIntervalKey(exerciseId, index)
                val intervalSeconds = setRestIntervals.firstOrNull {
                    it.exerciseId == exerciseId && it.afterSetIndex == index
                }?.durationSeconds ?: 90
                RestIntervalControl(
                    seconds = intervalSeconds,
                    expanded = expandedRestIntervalKey == intervalKey,
                    onToggle = { onToggleRestInterval(intervalKey) },
                    onChange = { onSetRestIntervalChanged(index, it) },
                    onStart = { onStartRestTimer(intervalKey, intervalSeconds, "dopo la serie ${index + 1}") },
                    isRunning = activeRestKey == intervalKey,
                    onStop = onStopRestTimer
                )
            }
        }
    }
}

@Composable
private fun BoxDay(day: Int, textColor: Color) {
    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
        Text(day.toString(), color = textColor)
    }
}

private sealed class AppRoute {
    data object Calendar : AppRoute()
    data class Workout(val date: CivilDate) : AppRoute()
    data object Templates : AppRoute()
    data object TemplateEditor : AppRoute()
    data object CloudBackup : AppRoute()
}

private data class CivilDate(val year: Int, val month: Int, val day: Int)
internal data class WorkoutForDate(
    val dateKey: String,
    val templateId: String,
    val templateName: String,
    val colorId: String,
    val exercises: List<WorkoutExercise>,
    val restDurationSeconds: Int = 90,
    val photoFileName: String? = null,
    val exerciseRestIntervals: List<ExerciseRestInterval> = emptyList(),
    val setRestIntervals: List<SetRestInterval> = emptyList()
)
internal data class WorkoutExercise(
    val id: String,
    val name: String,
    val baselineSetCount: Int,
    val isSkipped: Boolean = false,
    val isRemoved: Boolean = false,
    val isCustom: Boolean = false,
    val feedback: String? = null,
    val sets: List<WorkoutSet>,
    val groupId: String? = null,
    val groupType: ExerciseGroupType? = null,
    val notes: String? = null
)
internal data class WorkoutSet(
    val plannedRepetitions: String,
    val load: String = "",
    val completedRepetitions: String = "",
    val isWarmup: Boolean = false
)
private data class WorkoutSetHint(val load: String, val completedRepetitions: String)

private fun formatTimer(seconds: Int): String =
    "${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"

private fun remapSetRestIntervalsAfterRemoval(
    intervals: Map<String, Int>,
    exerciseId: String,
    removedSetIndex: Int,
    previousSetCount: Int
): Map<String, Int> {
    val updated = intervals.filterKeys { !it.startsWith("set\u001f$exerciseId\u001f") }.toMutableMap()
    for (newGapIndex in 0 until (previousSetCount - 2).coerceAtLeast(0)) {
        val oldGapIndex = when {
            newGapIndex < removedSetIndex - 1 -> newGapIndex
            newGapIndex >= removedSetIndex -> newGapIndex + 1
            else -> null
        } ?: continue
        intervals[setRestIntervalKey(exerciseId, oldGapIndex)]?.let { duration ->
            updated[setRestIntervalKey(exerciseId, newGapIndex)] = duration
        }
    }
    return updated
}

@Composable
private fun RestIntervalControl(
    seconds: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onChange: (Int) -> Unit,
    onStart: (() -> Unit)? = null,
    isRunning: Boolean = false,
    onStop: (() -> Unit)? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
            modifier = Modifier.size(34.dp).clickable(onClick = onToggle),
            shape = CircleShape,
            color = if (expanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "◷",
                    fontSize = 18.sp,
                    color = if (expanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (expanded) {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    TextButton(onClick = { onChange((seconds - 15).coerceAtLeast(15)) }) { Text("−15") }
                    Text(formatTimer(seconds), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { onChange((seconds + 15).coerceAtMost(600)) }) { Text("+15") }
                    if (onStart != null) {
                        if (isRunning && onStop != null) TextButton(onClick = onStop) { Text("Ferma") }
                        else Button(onClick = onStart) { Text("Avvia") }
                    }
                }
            }
        }
    }
}

private fun workoutPdfLines(workout: WorkoutForDate): List<String> = buildList {
    val dateParts = workout.dateKey.split('-')
    val day = dateParts.getOrNull(2)?.toIntOrNull()?.toString() ?: "—"
    val month = dateParts.getOrNull(1)?.toIntOrNull()?.let { monthNames.getOrNull(it - 1) } ?: ""
    val year = dateParts.getOrNull(0).orEmpty()
    add("Allenamento del $day $month $year")
    add("Seduta: ${workout.templateName}")
    add("")
    workout.exercises.forEachIndexed { index, exercise ->
        add("Esercizio ${index + 1}: ${exercise.name}")
        exercise.notes?.takeIf { it.isNotBlank() }?.let { add("Note: $it") }
        if (exercise.groupType != null) add("Gruppo: ${exercise.groupType.label}")
        when {
            exercise.isRemoved -> add("Rimosso da questo allenamento")
            exercise.isSkipped -> add("Esercizio saltato")
            else -> exercise.sets.forEachIndexed { setIndex, set ->
                val kind = if (set.isWarmup) "Avvicinamento" else "Lavoro"
                val load = set.load.takeIf { it.isNotBlank() }?.let { "$it kg" } ?: "non inserito"
                val completed = set.completedRepetitions.takeIf { it.isNotBlank() } ?: "non inserite"
                add("$kind ${setIndex + 1}: ${displayRepetitionTarget(set.plannedRepetitions)} previste · carico $load · $completed ripetizioni effettuate")
            }
        }
        add("")
    }
}

private fun previousWorkouts(current: WorkoutForDate, history: List<WorkoutForDate>): List<WorkoutForDate> =
    history.filter { previous ->
        previous.dateKey < current.dateKey && (
            if (previous.templateId.isNotEmpty() && current.templateId.isNotEmpty()) {
                previous.templateId == current.templateId
            } else {
                previous.templateName == current.templateName && previous.colorId == current.colorId
            }
        )
    }.sortedByDescending { it.dateKey }

private fun previousSetHints(
    current: WorkoutForDate,
    exercise: WorkoutExercise,
    history: List<WorkoutForDate>
): List<WorkoutSetHint> {
    val earlierExercises = previousWorkouts(current, history).mapNotNull { previous ->
        previous.exercises.firstOrNull { it.id == exercise.id && !it.isSkipped && !it.isRemoved }
    }
    return exercise.sets.mapIndexed { setIndex, _ ->
        val earlierSets = earlierExercises.mapNotNull { it.sets.getOrNull(setIndex) }
        WorkoutSetHint(
            load = earlierSets.firstOrNull { it.load.isNotBlank() }?.load.orEmpty(),
            completedRepetitions = earlierSets.firstOrNull { it.completedRepetitions.isNotBlank() }?.completedRepetitions.orEmpty()
        )
    }
}

private fun previousExerciseNote(
    current: WorkoutForDate,
    exercise: WorkoutExercise,
    history: List<WorkoutForDate>
): String? {
    val lastExercise = previousWorkouts(current, history)
        .asSequence()
        .mapNotNull { previous -> previous.exercises.firstOrNull { it.id == exercise.id } }
        .firstOrNull() ?: return null
    if (lastExercise.isSkipped || lastExercise.isRemoved) return "Saltato l'ultima volta"
    val change = lastExercise.sets.size - lastExercise.baselineSetCount
    return when {
        change > 0 -> "Ultima volta: ${lastExercise.sets.size} serie (+$change)"
        change < 0 -> "Ultima volta: ${lastExercise.sets.size} serie ($change)"
        else -> null
    }
}

private fun previousExerciseFeedback(
    current: WorkoutForDate,
    exercise: WorkoutExercise,
    history: List<WorkoutForDate>
): String? = previousWorkouts(current, history)
    .asSequence()
    .mapNotNull { previous -> previous.exercises.firstOrNull { it.id == exercise.id } }
    .firstOrNull()
    ?.feedback
    ?.takeIf { it.isNotBlank() }

private fun dateKey(date: CivilDate): String =
    "${date.year.toString().padStart(4, '0')}-${date.month.toString().padStart(2, '0')}-${date.day.toString().padStart(2, '0')}"

internal fun encodeWorkout(workout: WorkoutForDate): String = buildString {
    fun addField(value: String) { append(value.length).append(':').append(value) }
    append("W11|")
    addField(workout.dateKey)
    addField(workout.templateId)
    addField(workout.templateName)
    addField(workout.colorId)
    addField(workout.restDurationSeconds.toString())
    addField(workout.photoFileName.orEmpty())
    addField(workout.exercises.size.toString())
    workout.exercises.forEach { exercise ->
        addField(exercise.id)
        addField(exercise.name)
        addField(exercise.notes.orEmpty())
        addField(exercise.baselineSetCount.toString())
        addField(exercise.isSkipped.toString())
        addField(exercise.isRemoved.toString())
        addField(exercise.isCustom.toString())
        addField(exercise.feedback.orEmpty())
        addField(exercise.groupId.orEmpty())
        addField(exercise.groupType?.storageId.orEmpty())
        addField(exercise.sets.size.toString())
        exercise.sets.forEach { set ->
            addField(set.plannedRepetitions)
            addField(set.isWarmup.toString())
            addField(set.load)
            addField(set.completedRepetitions)
        }
    }
    addField(workout.exerciseRestIntervals.size.toString())
    workout.exerciseRestIntervals.forEach { interval ->
        addField(interval.afterExerciseId)
        addField(interval.beforeExerciseId)
        addField(interval.durationSeconds.toString())
    }
    addField(workout.setRestIntervals.size.toString())
    workout.setRestIntervals.forEach { interval ->
        addField(interval.exerciseId)
        addField(interval.afterSetIndex.toString())
        addField(interval.durationSeconds.toString())
    }
}

internal fun decodeWorkout(record: String): WorkoutForDate? = runCatching {
    val isFormat11 = record.startsWith("W11|")
    val isFormat10 = record.startsWith("W10|")
    val isFormat9 = record.startsWith("W9|")
    val isFormat8 = record.startsWith("W8|")
    val isFormat7 = record.startsWith("W7|")
    val isFormat6 = record.startsWith("W6|")
    val isFormat5 = record.startsWith("W5|")
    val isFormat4 = record.startsWith("W4|")
    val hasTemplateId = isFormat11 || isFormat10 || isFormat9 || isFormat8 || isFormat7 || isFormat6 || isFormat5 || isFormat4 || record.startsWith("W3|")
    val hasLogValues = hasTemplateId || record.startsWith("W2|")
    require(hasLogValues || record.startsWith("W1|"))
    val data = when {
        isFormat11 -> record.removePrefix("W11|")
        isFormat10 -> record.removePrefix("W10|")
        isFormat9 -> record.removePrefix("W9|")
        isFormat8 -> record.removePrefix("W8|")
        isFormat7 -> record.removePrefix("W7|")
        isFormat6 -> record.removePrefix("W6|")
        isFormat5 -> record.removePrefix("W5|")
        isFormat4 -> record.removePrefix("W4|")
        hasTemplateId -> record.removePrefix("W3|")
        hasLogValues -> record.removePrefix("W2|")
        else -> record.removePrefix("W1|")
    }
    var position = 0
    fun readField(): String {
        val separator = data.indexOf(':', position)
        require(separator >= position)
        val length = data.substring(position, separator).toInt()
        val start = separator + 1
        val end = start + length
        require(end <= data.length)
        position = end
        return data.substring(start, end)
    }
    val date = readField()
    val templateId = if (hasTemplateId) readField() else ""
    val templateName = readField()
    val colorId = readField()
    val restDurationSeconds = if (isFormat8 || isFormat9 || isFormat10 || isFormat11) readField().toInt().coerceIn(15, 600) else 90
    val photoFileName = if (isFormat9 || isFormat10 || isFormat11) readField().takeIf { it.isNotEmpty() } else null
    val count = readField().toInt()
    require(count in 1..100)
    val exercises = List(count) { exerciseIndex ->
        val id: String
        val name: String
        val baselineSetCount: Int
        val skipped: Boolean
        val removed: Boolean
        val custom: Boolean
        val feedback: String?
        val groupId: String?
        val groupType: ExerciseGroupType?
        val notes: String?
        if (isFormat4 || isFormat5 || isFormat6 || isFormat7 || isFormat8 || isFormat9 || isFormat10 || isFormat11) {
            id = readField()
            name = readField()
            notes = if (isFormat7 || isFormat8 || isFormat9 || isFormat10 || isFormat11) readField().takeIf { it.isNotEmpty() } else null
            baselineSetCount = readField().toInt()
            skipped = readField().toBooleanStrict()
            removed = readField().toBooleanStrict()
            custom = readField().toBooleanStrict()
            feedback = if (isFormat5 || isFormat6 || isFormat7 || isFormat8 || isFormat9 || isFormat10 || isFormat11) readField().takeIf { it.isNotEmpty() } else null
            groupId = if (isFormat6 || isFormat7 || isFormat8 || isFormat9 || isFormat10 || isFormat11) readField().takeIf { it.isNotEmpty() } else null
            groupType = if (isFormat6 || isFormat7 || isFormat8 || isFormat9 || isFormat10 || isFormat11) exerciseGroupTypeFromStorage(readField()) else null
        } else {
            name = readField()
            id = if (templateId.isNotEmpty()) "$templateId-exercise-$exerciseIndex" else "legacy-exercise-$exerciseIndex"
            baselineSetCount = -1
            skipped = false
            removed = false
            custom = false
            feedback = null
            groupId = null
            groupType = null
            notes = null
        }
        val setCount = readField().toInt()
        require(setCount in 1..100)
        val sets = List(setCount) {
            val planned = readField()
            if (hasLogValues) {
                val warmup = if (isFormat7 || isFormat8 || isFormat9 || isFormat10 || isFormat11) readField().toBooleanStrict() else false
                WorkoutSet(planned, readField(), readField(), warmup)
            } else WorkoutSet(planned)
        }
        WorkoutExercise(
            id, name, baselineSetCount.takeIf { it >= 0 } ?: setCount,
            skipped, removed, custom, feedback, sets, groupId, groupType, notes
        )
    }
    val restIntervals = if (isFormat10 || isFormat11) {
        val intervalCount = readField().toInt()
        require(intervalCount in 0..99)
        List(intervalCount) { ExerciseRestInterval(readField(), readField(), readField().toInt().coerceIn(15, 600)) }
    } else emptyList()
    val setRestIntervals = if (isFormat11) {
        val intervalCount = readField().toInt()
        require(intervalCount in 0..10_000)
        List(intervalCount) {
            SetRestInterval(readField(), readField().toInt().coerceAtLeast(0), readField().toInt().coerceIn(15, 600))
        }
    } else if (restDurationSeconds != 90) {
        exercises.flatMap { exercise ->
            (0 until exercise.sets.lastIndex).map { index -> SetRestInterval(exercise.id, index, restDurationSeconds) }
        }
    } else emptyList()
    require(position == data.length)
    WorkoutForDate(date, templateId, templateName, colorId, exercises, restDurationSeconds, photoFileName, restIntervals, setRestIntervals)
}.getOrNull()

// Converte il numero di giorni trascorsi dal 1970-01-01 in una data gregoriana.
private fun civilDate(epochDay: Long): CivilDate {
    val z = epochDay + 719468
    val era = if (z >= 0) z / 146097 else (z - 146096) / 146097
    val dayOfEra = z - era * 146097
    val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365
    var year = yearOfEra + era * 400
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthPrime = (5 * dayOfYear + 2) / 153
    val day = dayOfYear - (153 * monthPrime + 2) / 5 + 1
    val month = monthPrime + if (monthPrime < 10) 3 else -9
    if (month <= 2) year++
    return CivilDate(year.toInt(), month.toInt(), day.toInt())
}

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

private fun weekdayMondayFirst(year: Int, month: Int): Int {
    val adjustedYear = year - if (month <= 2) 1 else 0
    val era = adjustedYear / 400
    val yearOfEra = adjustedYear - era * 400
    val monthAdjusted = month + if (month > 2) -3 else 9
    val dayOfYear = (153 * monthAdjusted + 2) / 5
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    val epochDay = era * 146097L + dayOfEra - 719468
    return ((epochDay + 3).mod(7)).toInt()
}
