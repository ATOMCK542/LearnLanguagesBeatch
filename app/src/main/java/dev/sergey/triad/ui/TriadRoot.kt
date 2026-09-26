package dev.sergey.triad.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.sergey.triad.BuildConfig
import dev.sergey.triad.R
import dev.sergey.triad.domain.AnswerEvaluator
import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.Concept
import dev.sergey.triad.domain.Exercise
import dev.sergey.triad.domain.LessonBlock
import dev.sergey.triad.domain.PracticePlanner
import dev.sergey.triad.domain.Theme
import dev.sergey.triad.domain.Profile
import dev.sergey.triad.domain.WidgetKind
import dev.sergey.triad.ui.theme.lessonPalette
import dev.sergey.triad.ui.theme.triadCardColors
import dev.sergey.triad.ui.widget.WidgetPinner
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun TriadRoot(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val context = LocalContext.current
    val askNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.setReminderEnabled(granted)
    }
    LaunchedEffect(state.ready, state.reminderReady, state.reminderEnabled, state.reminderPrompted) {
        if (Build.VERSION.SDK_INT < 33) return@LaunchedEffect
        if (!state.ready || !state.reminderReady || !state.reminderEnabled || state.reminderPrompted) return@LaunchedEffect
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        viewModel.markReminderPrompted()
        if (!granted) askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (!state.ready) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding()
                    .testTag("loading"),
            )
            return@Surface
        }
        if (state.active == null) {
            OnboardingScreen(onCreate = viewModel::createProfile)
            return@Surface
        }
        NavHost(navController = nav, startDestination = "main", modifier = Modifier.fillMaxSize()) {
            composable("main") { MainTabs(nav, state, viewModel) }
            composable("session") { SessionScreen(state, viewModel, onClose = { nav.popBackStack() }) }
            composable("onboarding") {
                OnboardingScreen(onCreate = { n, e, l, t ->
                    viewModel.createProfile(n, e, l, t)
                    nav.popBackStack()
                })
            }
            composable("card/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val concept = state.concepts.firstOrNull { it.id == id }
                if (concept == null) {
                    nav.popBackStack()
                } else {
                    CardDetailScreen(concept, state, viewModel) { nav.popBackStack() }
                }
            }
            composable("translator") { TranslatorScreen { nav.popBackStack() } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTabs(nav: NavHostController, state: MainUiState, vm: MainViewModel) {
    var tab by remember { mutableStateOf(0) }
    val titles = listOf(R.string.nav_study, R.string.nav_path, R.string.nav_cards, R.string.nav_more)
    val icons = listOf(Icons.Outlined.School, Icons.Outlined.Category, Icons.AutoMirrored.Outlined.MenuBook, Icons.Outlined.MoreHoriz)
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { TopAppBar(title = { Text(stringResource(titles[tab])) }) },
        bottomBar = {
            NavigationBar(windowInsets = WindowInsets.navigationBars) {
                titles.forEachIndexed { index, res ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(icons[index], contentDescription = stringResource(res)) },
                        label = { Text(stringResource(res)) },
                    )
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().imePadding()) {
            when (tab) {
                0 -> StudyPane(state, vm) { nav.navigate("session") }
                1 -> PathPane(state, vm)
                2 -> LibraryPane(state, vm, nav)
                else -> MorePane(state, vm, nav)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudyPane(state: MainUiState, vm: MainViewModel, onStart: () -> Unit) {
    val profile = state.active ?: return
    Column(Modifier.padding(screenGutter()).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(profile.displayName, style = MaterialTheme.typography.headlineSmall)
        Text(pluralStringResource(R.plurals.due_count, state.due, state.due))
        Text(pluralStringResource(R.plurals.streak_days, profile.streakDays, profile.streakDays))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            profile.studyTargets().forEach { Text(it.code.uppercase(), style = MaterialTheme.typography.labelLarge) }
        }
        val selectedTheme = state.themes.firstOrNull { it.id == state.selectedThemeId }
        if (selectedTheme != null) {
            Text(
                stringResource(R.string.theme_studying, selectedTheme.title.forLang(profile.uiLang)),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Button(onClick = {
            vm.startSession { onStart() }
        }, enabled = !state.planningSession, modifier = Modifier.testTag("start_session")) {
            Text(stringResource(R.string.action_start))
        }
        Text(stringResource(R.string.review_title), style = MaterialTheme.typography.titleMedium)
        if (state.practiceAvailable == 0) {
            Text(
                stringResource(R.string.review_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(pluralStringResource(R.plurals.review_ready, state.practiceAvailable, state.practiceAvailable))
            Text(stringResource(R.string.review_size), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PracticePlanner.sizeChoices(state.practiceAvailable).forEach { size ->
                    FilterChip(
                        selected = state.practiceSize == size,
                        onClick = { vm.setPracticeSize(size) },
                        label = { Text(size.toString()) },
                    )
                }
            }
            Button(
                onClick = {
                    vm.startPractice()
                    onStart()
                },
                enabled = state.practiceSize > 0,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("start_review"),
            ) {
                Text(stringResource(R.string.action_review))
            }
        }
        WidgetPinRow()
    }
}

@Composable
private fun WidgetPinRow() {
    val context = LocalContext.current
    val pinSupported = remember { WidgetPinner.supported(context) }
    Text(stringResource(R.string.widget_home_title), style = MaterialTheme.typography.titleMedium)
    if (pinSupported) {
        Button(
            onClick = { WidgetPinner.request(context, WidgetKind.Lesson) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(stringResource(R.string.widget_add_lesson))
        }
        Button(
            onClick = { WidgetPinner.request(context, WidgetKind.Review) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(stringResource(R.string.widget_add_review))
        }
    } else {
        Text(
            stringResource(R.string.widget_pin_unsupported),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PathPane(state: MainUiState, vm: MainViewModel) {
    val gutter = screenGutter()
    val uiLang = state.active?.uiLang ?: AppLanguage.En
    val targets = state.active?.studyTargets()?.toSet().orEmpty()
    val visible = state.themes.filter { theme ->
        val primer = theme.primerLanguage()
        primer == null || primer in targets
    }
    var openThemes by remember { mutableStateOf(setOf<String>()) }
    var openSections by remember { mutableStateOf(setOf<String>()) }
    val ttsOn = state.active?.ttsEnabled == true
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(gutter),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(visible, key = { it.id }) { theme ->
            if (theme.kind == "primer") {
                PrimerThemeCard(
                    theme = theme,
                    uiLang = uiLang,
                    gutter = gutter,
                    open = theme.id in openThemes,
                    onToggle = {
                        openThemes = if (theme.id in openThemes) openThemes - theme.id else openThemes + theme.id
                    },
                    sectionOpen = { sectionId -> "${theme.id}/$sectionId" in openSections },
                    onToggleSection = { sectionId ->
                        val key = "${theme.id}/$sectionId"
                        openSections = if (key in openSections) openSections - key else openSections + key
                    },
                    ttsOn = ttsOn,
                    canSpeak = vm::ttsAvailable,
                    onSpeak = { text, lang -> vm.speak(text, lang) },
                )
            } else {
                UnitThemeCard(theme, state, vm, gutter, uiLang)
            }
        }
    }
}

@Composable
private fun UnitThemeCard(
    theme: Theme,
    state: MainUiState,
    vm: MainViewModel,
    gutter: Dp,
    uiLang: AppLanguage,
) {
    val selected = theme.id == state.selectedThemeId
    val description = theme.description.forLang(uiLang)
    val study = state.themeStudy[theme.id]
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = triadCardColors(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(gutter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    theme.title.forLang(uiLang),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (description.isNotBlank()) {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (study != null && study.total > 0) {
                    Text(
                        if (study.studied) {
                            stringResource(R.string.theme_studied)
                        } else {
                            stringResource(R.string.theme_progress, study.covered, study.total)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (study.studied) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.theme_study),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Switch(
                    checked = selected,
                    onCheckedChange = { on -> vm.setThemeStudying(theme.id, on) },
                )
            }
        }
    }
}

@Composable
private fun PrimerThemeCard(
    theme: Theme,
    uiLang: AppLanguage,
    gutter: Dp,
    open: Boolean,
    onToggle: () -> Unit,
    sectionOpen: (String) -> Boolean,
    onToggleSection: (String) -> Unit,
    ttsOn: Boolean,
    canSpeak: (AppLanguage) -> Boolean,
    onSpeak: (String, AppLanguage) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = triadCardColors(),
    ) {
        Column(Modifier.fillMaxWidth().padding(gutter), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    theme.title.forLang(uiLang),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (open) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = stringResource(
                        if (open) R.string.theme_collapse else R.string.theme_expand,
                    ),
                )
            }
            if (open) {
                val description = theme.description.forLang(uiLang)
                if (description.isNotBlank()) {
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                theme.sections.forEach { section ->
                    val expanded = sectionOpen(section.id)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable { onToggleSection(section.id) },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                section.title.forLang(uiLang),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = stringResource(
                                    if (expanded) R.string.theme_collapse else R.string.theme_expand,
                                ),
                            )
                        }
                        if (expanded) {
                            section.blocks.forEach { block ->
                                when (block) {
                                    is LessonBlock.Paragraph -> Text(
                                        block.text.forLang(uiLang),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    is LessonBlock.Speak -> SpeakExample(block, uiLang, ttsOn, canSpeak, onSpeak)
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
private fun SpeakExample(
    block: LessonBlock.Speak,
    uiLang: AppLanguage,
    ttsOn: Boolean,
    canSpeak: (AppLanguage) -> Boolean,
    onSpeak: (String, AppLanguage) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                block.say,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            val caption = block.caption.forLang(uiLang)
            if (caption.isNotBlank()) {
                Text(
                    caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (ttsOn && canSpeak(block.lang)) {
            IconButton(onClick = { onSpeak(block.say, block.lang) }) {
                Icon(
                    Icons.AutoMirrored.Outlined.VolumeUp,
                    contentDescription = stringResource(R.string.label_speak),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryPane(state: MainUiState, vm: MainViewModel, nav: NavHostController) {
    var en by remember { mutableStateOf("") }
    var ru by remember { mutableStateOf("") }
    var vi by remember { mutableStateOf("") }
    val q = state.query
    val native = state.active?.nativeLang ?: AppLanguage.Ru
    val ttsOn = state.active?.ttsEnabled == true
    val filtered = state.concepts.filter {
        q.isBlank() || it.texts.values.any { t -> t.text.contains(q, ignoreCase = true) }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(screenGutter()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            OutlinedTextField(q, vm::setQuery, label = { Text(stringResource(R.string.search)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(en, { en = it }, label = { Text(stringResource(R.string.text_en)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(ru, { ru = it }, label = { Text(stringResource(R.string.text_ru)) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(vi, { vi = it }, label = { Text(stringResource(R.string.text_vi)) }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                if (en.isNotBlank() && ru.isNotBlank() && vi.isNotBlank()) {
                    vm.addCard(en, ru, vi)
                    en = ""; ru = ""; vi = ""
                }
            }) { Text(stringResource(R.string.add_card)) }
        }
        items(filtered, key = { it.id }) { concept ->
            val nativeForm = concept.text(native)
            Card(
                onClick = { nav.navigate("card/${concept.id}") },
                modifier = Modifier.fillMaxWidth(),
                colors = triadCardColors(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            nativeForm.text,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f),
                        )
                        if (ttsOn) {
                            IconButton(onClick = { vm.speak(nativeForm.text, native) }) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.VolumeUp,
                                    contentDescription = stringResource(R.string.label_speak),
                                )
                            }
                        }
                    }
                    if (nativeForm.ipa.isNotBlank()) {
                        Text(
                            nativeForm.ipa,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    AppLanguage.all.filter { it != native }.forEach { lang ->
                        val form = concept.text(lang)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "${endonym(lang)}: ${form.text}",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                if (form.ipa.isNotBlank()) {
                                    Text(
                                        form.ipa,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                            if (ttsOn) {
                                IconButton(onClick = { vm.speak(form.text, lang) }) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.VolumeUp,
                                        contentDescription = stringResource(R.string.label_speak),
                                    )
                                }
                            }
                        }
                    }
                    FilterChip(
                        selected = concept.id in state.masteredIds,
                        onClick = { vm.toggleMastered(concept.id) },
                        label = { Text(stringResource(R.string.mark_learned)) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardDetailScreen(
    concept: Concept,
    state: MainUiState,
    vm: MainViewModel,
    onBack: () -> Unit,
) {
    val native = state.active?.nativeLang ?: AppLanguage.Ru
    val ttsOn = state.active?.ttsEnabled == true
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(concept.text(native).text) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FilterChip(
                selected = concept.id in state.masteredIds,
                onClick = { vm.toggleMastered(concept.id) },
                label = { Text(stringResource(R.string.mark_learned)) },
            )
            AppLanguage.all.forEach { lang ->
                val form = concept.text(lang)
                LessonPhraseCard(
                    lang = lang,
                    text = form.text,
                    ipa = form.ipa,
                    speakEnabled = ttsOn,
                    onSpeak = { vm.speak(form.text, lang) },
                    highlighted = lang != native,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MorePane(state: MainUiState, vm: MainViewModel, nav: NavHostController) {
    val profile = state.active ?: return
    var pendingExport by remember { mutableStateOf<String?>(null) }
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val body = pendingExport ?: return@rememberLauncherForActivityResult
        if (uri != null) vm.writeExport(uri, body)
    }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importFrom(uri)
    }
    var confirmDelete by remember { mutableStateOf<Profile?>(null) }
    Column(Modifier.padding(screenGutter()).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.accounts), style = MaterialTheme.typography.titleLarge)
        state.profiles.forEach { p ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(p.displayName + if (p.id == profile.id) " ✓" else "")
                Row {
                    TextButton(onClick = { vm.switchProfile(p.id) }) { Text(stringResource(R.string.switch_account)) }
                    TextButton(onClick = { confirmDelete = p }) { Text(stringResource(R.string.delete_account)) }
                }
            }
        }
        Button(onClick = { nav.navigate("onboarding") }) { Text(stringResource(R.string.add_account)) }
        Text(stringResource(R.string.settings_ui))
        LanguageRow(profile.uiLang) { lang -> vm.updateActive { it.copy(uiLang = lang) } }
        Text(stringResource(R.string.settings_native))
        LanguageRow(profile.nativeLang) { lang ->
            vm.updateActive { p ->
                p.copy(
                    nativeLang = lang,
                    targetLangs = p.targetLangs.filter { it != lang }.ifEmpty {
                        AppLanguage.all.filter { it != lang }
                    },
                )
            }
        }
        Text(stringResource(R.string.settings_targets))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.all.filter { it != profile.nativeLang }.forEach { lang ->
                val selected = lang in profile.targetLangs
                FilterChip(selected, onClick = {
                    vm.updateActive { p ->
                        val next = if (selected) p.targetLangs - lang else p.targetLangs + lang
                        p.copy(targetLangs = next.filter { it != p.nativeLang }.ifEmpty { listOf(lang) })
                    }
                }, label = { Text(endonym(lang)) })
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.settings_tts))
            Switch(checked = profile.ttsEnabled, onCheckedChange = { on -> vm.updateActive { it.copy(ttsEnabled = on) } })
        }
        Text(
            stringResource(R.string.settings_tts_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val reminderContext = LocalContext.current
        val askReminder = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            vm.setReminderEnabled(granted)
        }
        var pickingReminderTime by remember { mutableStateOf(false) }
        val reminderTime = remember(profile.uiLang, state.reminderTime) {
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(Locale.forLanguageTag(profile.uiLang.code))
                .format(state.reminderTime)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.settings_reminder), modifier = Modifier.weight(1f))
            Switch(
                checked = state.reminderEnabled,
                onCheckedChange = { on ->
                    if (!on) {
                        vm.setReminderEnabled(false)
                    } else if (notificationPermissionGranted(reminderContext)) {
                        vm.setReminderEnabled(true)
                    } else {
                        requestNotificationPermission(reminderContext, state.reminderPrompted) {
                            askReminder.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                },
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.settings_reminder_time), modifier = Modifier.weight(1f))
            TextButton(onClick = { pickingReminderTime = true }) { Text(reminderTime) }
        }
        Text(
            stringResource(R.string.settings_reminder_hint, reminderTime),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (pickingReminderTime) {
            ReminderTimeDialog(
                initial = state.reminderTime,
                is24Hour = DateFormat.is24HourFormat(reminderContext),
                onDismiss = { pickingReminderTime = false },
                onConfirm = { time ->
                    pickingReminderTime = false
                    vm.setReminderTime(time)
                },
            )
        }
        UnlockSettings(state, vm)
        Text(stringResource(R.string.stats_title), style = MaterialTheme.typography.titleMedium)
        Text(pluralStringResource(R.plurals.reviews_done, profile.reviewsDone, profile.reviewsDone))
        Button(onClick = { vm.export(false) { pendingExport = it; create.launch("triad-profile.json") } }) {
            Text(stringResource(R.string.export_current))
        }
        Button(onClick = { vm.export(true) { pendingExport = it; create.launch("triad-all.json") } }) {
            Text(stringResource(R.string.export_all))
        }
        Button(onClick = { open.launch(arrayOf("application/json", "*/*")) }) { Text(stringResource(R.string.import_backup)) }
        Button(onClick = { nav.navigate("translator") }) { Text(stringResource(R.string.translator_title)) }
        Text(
            stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    confirmDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            confirmButton = {
                TextButton(onClick = { vm.deleteProfile(p.id); confirmDelete = null }) {
                    Text(stringResource(R.string.delete_account))
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text(stringResource(R.string.cancel)) } },
            text = { Text(stringResource(R.string.confirm_delete)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SessionScreen(
    state: MainUiState,
    vm: MainViewModel,
    compact: Boolean = false,
    onClose: () -> Unit,
) {
    val item = vm.currentItem()
    val padH = if (compact) 12.dp else 16.dp
    val buttonMin = if (compact) 44.dp else 52.dp
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (item == null) {
                            stringResource(R.string.nav_study)
                        } else {
                            stringResource(
                                R.string.session_progress,
                                state.sessionIndex + 1,
                                state.session.size,
                            )
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        bottomBar = {
            Column(Modifier.windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))) {
                when {
                    item == null -> {
                        SessionBottomBar {
                            Button(
                                onClick = onClose,
                                modifier = Modifier.fillMaxWidth().heightIn(min = buttonMin),
                            ) {
                                Text(
                                    stringResource(if (compact) R.string.widget_close else R.string.session_back),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    }
                    state.revealed -> {
                        SessionBottomBar {
                            Button(
                                onClick = vm::next,
                                modifier = Modifier.fillMaxWidth().heightIn(min = buttonMin).testTag("next_card"),
                            ) {
                                Text(stringResource(R.string.action_next), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        if (item == null) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(if (state.session.isEmpty()) R.string.empty_session else R.string.session_done),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                )
            }
            return@Scaffold
        }
        val native = item.exercise.nativeLang
        val concept = item.exercise.concept
        val nativeForm = concept.text(native)
        val grammar = concept.grammar.forLang(native)
        val ttsOn = state.active?.ttsEnabled == true
        val speakPick: (String, AppLanguage) -> Unit = { text, lang ->
            if (ttsOn && text.isNotBlank()) vm.speak(text, lang, openSettingsIfMissing = false)
        }
        val palette = lessonPalette()
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = padH, vertical = if (compact) 4.dp else 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LinearProgressIndicator(
                progress = { (state.sessionIndex + 1f) / state.session.size.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth(),
            )
            LessonPrompt(
                lang = native,
                text = nativeForm.text,
                speakEnabled = ttsOn,
                onSpeak = { vm.speak(nativeForm.text, native) },
            )
            if (grammar.isNotBlank()) {
                Text(
                    grammar,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.lastCorrect?.let { ok ->
                if (state.revealed) {
                    Text(
                        stringResource(if (ok) R.string.correct else R.string.incorrect),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (ok) palette.onCorrect else palette.onWrong,
                    )
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (val ex = item.exercise) {
                    is Exercise.Cloze -> {
                        ex.banks.forEach { bank ->
                            val form = concept.text(bank.lang)
                            Text(
                                stringResource(R.string.pick_in_language, endonym(bank.lang)),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            )
                            QuizOptions(
                                options = bank.options,
                                correct = bank.correct,
                                picked = state.quizPicks[bank.lang],
                                revealed = state.revealed,
                                ipa = form.ipa,
                                speakEnabled = ttsOn,
                                onSpeak = { vm.speak(form.text, bank.lang) },
                                onPick = { option ->
                                    speakPick(option, bank.lang)
                                    vm.pickQuiz(bank.lang, option)
                                },
                            )
                        }
                    }
                    is Exercise.OrderChips -> {
                        if (state.revealed) {
                            val sentence = ex.correct.joinToString(" ")
                            val form = concept.text(ex.targetLang)
                            val options = listOfNotNull(
                                sentence,
                                state.lastPicked?.takeIf { it != sentence },
                            )
                            QuizOptions(
                                options = options,
                                correct = sentence,
                                picked = state.lastPicked,
                                revealed = true,
                                ipa = form.ipa,
                                speakEnabled = ttsOn,
                                onSpeak = { vm.speak(form.text, ex.targetLang) },
                                onPick = {},
                            )
                        } else {
                            OrderExercise(
                                ex,
                                vm,
                                onSpeakChip = { chip -> speakPick(chip, ex.targetLang) },
                            )
                        }
                    }
                    is Exercise.TonePick -> {
                        QuizOptions(
                            options = ex.options,
                            correct = ex.correct,
                            picked = state.lastPicked,
                            revealed = state.revealed,
                            onPick = { option ->
                                speakPick(option, ex.targetLang)
                                vm.answerGame(option == ex.correct, option)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionBottomBar(content: @Composable () -> Unit) {
    Surface(tonalElevation = 4.dp, shadowElevation = 6.dp) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun LessonPrompt(
    lang: AppLanguage,
    text: String,
    speakEnabled: Boolean,
    onSpeak: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                endonym(lang),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.testTag("prompt"),
            )
        }
        if (speakEnabled) {
            IconButton(onClick = onSpeak) {
                Icon(
                    Icons.AutoMirrored.Outlined.VolumeUp,
                    contentDescription = stringResource(R.string.label_speak),
                )
            }
        }
    }
}

@Composable
private fun LessonPhraseCard(
    lang: AppLanguage,
    text: String,
    ipa: String = "",
    speakEnabled: Boolean,
    onSpeak: () -> Unit,
    highlighted: Boolean,
    promptTag: Boolean = false,
) {
    val colors = if (highlighted) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        triadCardColors()
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlighted) 6.dp else 3.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    endonym(lang),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (highlighted) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
                Spacer(Modifier.weight(1f))
                if (speakEnabled) {
                    IconButton(onClick = onSpeak) {
                        Icon(
                            Icons.AutoMirrored.Outlined.VolumeUp,
                            contentDescription = stringResource(R.string.label_speak),
                        )
                    }
                }
            }
            Text(
                text,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                modifier = if (promptTag) Modifier.testTag("prompt") else Modifier,
            )
            if (ipa.isNotBlank()) {
                Text(
                    ipa,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                )
            }
        }
    }
}

@Composable
private fun QuizOptions(
    options: List<String>,
    correct: String,
    picked: String?,
    revealed: Boolean,
    ipa: String = "",
    speakEnabled: Boolean = false,
    onSpeak: () -> Unit = {},
    onPick: (String) -> Unit,
) {
    val palette = lessonPalette()
    options.forEach { option ->
        val isCorrect = revealed && option == correct
        val isWrongPick = revealed && option == picked && option != correct
        val colors = when {
            isCorrect -> ButtonDefaults.filledTonalButtonColors(
                containerColor = palette.correctContainer,
                contentColor = palette.onCorrect,
                disabledContainerColor = palette.correctContainer,
                disabledContentColor = palette.onCorrect,
            )
            isWrongPick -> ButtonDefaults.filledTonalButtonColors(
                containerColor = palette.wrongContainer,
                contentColor = palette.onWrong,
                disabledContainerColor = palette.wrongContainer,
                disabledContentColor = palette.onWrong,
            )
            !revealed && option == picked -> ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            else -> ButtonDefaults.filledTonalButtonColors()
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = { onPick(option) },
                enabled = !revealed,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = colors,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(option, style = MaterialTheme.typography.titleMedium)
                    if (isCorrect && ipa.isNotBlank()) {
                        Text(ipa, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (isCorrect && speakEnabled) {
                IconButton(onClick = onSpeak) {
                    Icon(
                        Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = stringResource(R.string.label_speak),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderExercise(
    ex: Exercise.OrderChips,
    vm: MainViewModel,
    onSpeakChip: (String) -> Unit = {},
) {
    var built by remember { mutableStateOf(emptyList<String>()) }
    var pool by remember { mutableStateOf(ex.shuffled) }
    Text(
        stringResource(R.string.build_sentence),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = triadCardColors(),
    ) {
        FlowRow(
            modifier = Modifier.padding(16.dp).fillMaxWidth().heightIn(min = 56.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (built.isEmpty()) {
                Text("…", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                built.forEach { chip ->
                    Text(chip, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pool.forEach { chip ->
            FilledTonalButton(onClick = {
                vm.playClick()
                onSpeakChip(chip)
                built = built + chip
                pool = pool - chip
            }) { Text(chip, style = MaterialTheme.typography.titleMedium) }
        }
    }
    Button(
        onClick = {
            vm.answerGame(
                AnswerEvaluator.chipsMatch(ex.correct, built, ex.targetLang),
                built.joinToString(" "),
            )
        },
        enabled = pool.isEmpty(),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
    ) { Text(stringResource(R.string.action_check), style = MaterialTheme.typography.titleMedium) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingScreen(
    onCreate: (String, String?, AppLanguage, List<AppLanguage>) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var ui by remember { mutableStateOf<AppLanguage?>(null) }
    var targets by remember { mutableStateOf(setOf<AppLanguage>()) }
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(screenGutter())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.onboarding_name)) }, modifier = Modifier.fillMaxWidth().testTag("name"))
        OutlinedTextField(email, { email = it }, label = { Text(stringResource(R.string.onboarding_email)) }, modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.onboarding_language), color = MaterialTheme.colorScheme.onSurface)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.all.forEach { lang ->
                FilterChip(selected = ui == lang, onClick = {
                    ui = lang
                    targets = AppLanguage.all.filter { it != lang }.toSet()
                }, label = { Text(endonym(lang)) })
            }
        }
        Text(stringResource(R.string.onboarding_targets), color = MaterialTheme.colorScheme.onSurface)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.all.filter { it != ui }.forEach { lang ->
                FilterChip(selected = lang in targets, onClick = {
                    targets = if (lang in targets) targets - lang else targets + lang
                }, label = { Text(endonym(lang)) })
            }
        }
        Button(
            enabled = ui != null && targets.isNotEmpty(),
            onClick = { onCreate(name, email.ifBlank { null }, ui!!, targets.toList()) },
            modifier = Modifier.testTag("create_profile"),
        ) { Text(stringResource(R.string.action_create)) }
    }
}

@Composable
private fun TranslatorScreen(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(screenGutter()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.translator_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            stringResource(R.string.translator_stub),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Button(onClick = onBack) { Text(stringResource(R.string.action_continue)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LanguageRow(selected: AppLanguage, onPick: (AppLanguage) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppLanguage.all.forEach { lang ->
            FilterChip(selected = selected == lang, onClick = { onPick(lang) }, label = { Text(endonym(lang)) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(
    initial: LocalTime,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val picker = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = is24Hour,
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.settings_reminder_time), style = MaterialTheme.typography.titleLarge)
                TimePicker(state = picker)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    TextButton(onClick = { onConfirm(LocalTime.of(picker.hour, picker.minute)) }) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

private fun notificationPermissionGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < 33) return true
    return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
}

private fun requestNotificationPermission(
    context: Context,
    prompted: Boolean,
    launch: () -> Unit,
) {
    if (Build.VERSION.SDK_INT < 33) {
        launch()
        return
    }
    val activity = context as? Activity
    val showRationale = activity != null &&
        ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
    if (prompted && !showRationale) {
        context.startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
        )
    } else {
        launch()
    }
}

@Composable
private fun endonym(lang: AppLanguage): String = stringResource(
    when (lang) {
        AppLanguage.En -> R.string.lang_en_endonym
        AppLanguage.Ru -> R.string.lang_ru_endonym
        AppLanguage.Vi -> R.string.lang_vi_endonym
    },
)

@Composable
private fun screenGutter(): Dp {
    val width = LocalConfiguration.current.screenWidthDp
    return if (width < 380) 12.dp else 16.dp
}
