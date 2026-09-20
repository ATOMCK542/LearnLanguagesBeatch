package dev.sergey.triad.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Route
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.sergey.triad.R
import dev.sergey.triad.domain.AnswerEvaluator
import dev.sergey.triad.domain.AppLanguage
import dev.sergey.triad.domain.Concept
import dev.sergey.triad.domain.Exercise
import dev.sergey.triad.domain.Profile
import dev.sergey.triad.domain.Rating

@Composable
fun TriadRoot(viewModel: MainViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    if (!state.ready) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().testTag("loading"))
        return
    }
    if (state.active == null) {
        OnboardingScreen(onCreate = viewModel::createProfile)
        return
    }
    NavHost(navController = nav, startDestination = "main") {
        composable("main") { MainTabs(nav, state, viewModel) }
        composable("session") { SessionScreen(nav, state, viewModel) }
        composable("onboarding") {
            OnboardingScreen(onCreate = { n, e, l, t ->
                viewModel.createProfile(n, e, l, t)
                nav.popBackStack()
            })
        }
        composable("primer/{id}") { entry ->
            PrimerScreen(entry.arguments?.getString("id").orEmpty(), viewModel) { nav.popBackStack() }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTabs(nav: NavHostController, state: MainUiState, vm: MainViewModel) {
    var tab by remember { mutableStateOf(0) }
    val titles = listOf(R.string.nav_study, R.string.nav_path, R.string.nav_cards, R.string.nav_more)
    val icons = listOf(Icons.Outlined.School, Icons.Outlined.Route, Icons.AutoMirrored.Outlined.MenuBook, Icons.Outlined.MoreHoriz)
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(titles[tab])) }) },
        bottomBar = {
            NavigationBar {
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
        Column(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                0 -> StudyPane(state, vm) { nav.navigate("session") }
                1 -> PathPane(state, nav)
                2 -> LibraryPane(state, vm, nav)
                else -> MorePane(state, vm, nav)
            }
        }
    }
}

@Composable
private fun StudyPane(state: MainUiState, vm: MainViewModel, onStart: () -> Unit) {
    val profile = state.active ?: return
    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(profile.displayName, style = MaterialTheme.typography.headlineSmall)
        Text(pluralStringResource(R.plurals.due_count, state.due, state.due))
        Text(pluralStringResource(R.plurals.streak_days, profile.streakDays, profile.streakDays))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            profile.targetLangs.forEach { Text(it.code.uppercase(), style = MaterialTheme.typography.labelLarge) }
        }
        Button(onClick = {
            vm.startSession()
            onStart()
        }, modifier = Modifier.testTag("start_session")) {
            Text(stringResource(R.string.action_start))
        }
    }
}

@Composable
private fun PathPane(state: MainUiState, nav: NavHostController) {
    LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.themes, key = { it.id }) { theme ->
            val lang = state.active?.uiLang ?: AppLanguage.En
            val open = theme.id in state.unlocked || theme.kind == "primer"
            Card(onClick = {
                if (theme.kind == "primer") nav.navigate("primer/${theme.id}")
            }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(theme.title.forLang(lang), style = MaterialTheme.typography.titleMedium)
                    Text(if (open) stringResource(R.string.unlocked) else stringResource(R.string.locked))
                }
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
    LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
        LanguageRow(profile.nativeLang) { lang -> vm.updateActive { it.copy(nativeLang = lang) } }
        Text(stringResource(R.string.settings_targets))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.all.forEach { lang ->
                val selected = lang in profile.targetLangs
                FilterChip(selected, onClick = {
                    vm.updateActive { p ->
                        val next = if (selected) p.targetLangs - lang else p.targetLangs + lang
                        p.copy(targetLangs = next.ifEmpty { listOf(lang) })
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
private fun SessionScreen(nav: NavHostController, state: MainUiState, vm: MainViewModel) {
    val item = vm.currentItem()
    Scaffold(
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
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
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
                Button(
                    onClick = { nav.popBackStack() },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                ) { Text(stringResource(R.string.action_continue), style = MaterialTheme.typography.titleMedium) }
            }
            return@Scaffold
        }
        val native = item.exercise.nativeLang
        val target = item.exercise.targetLang
        val concept = item.exercise.concept
        val other = AppLanguage.all.first { it != native && it != target }
        val ttsOn = state.active?.ttsEnabled == true
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LinearProgressIndicator(
                progress = { (state.sessionIndex + 1f) / state.session.size.coerceAtLeast(1) },
                modifier = Modifier.fillMaxWidth(),
            )
            LessonPhraseCard(
                lang = native,
                text = concept.text(native).text,
                speakEnabled = ttsOn,
                onSpeak = { vm.speak(concept.text(native).text, native) },
                highlighted = false,
                promptTag = true,
            )
            when (val ex = item.exercise) {
                is Exercise.Reveal -> {
                    if (!state.revealed) {
                        Button(
                            onClick = vm::reveal,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                        ) {
                            Text(stringResource(R.string.action_show), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                is Exercise.Cloze -> {
                    Text(
                        stringResource(R.string.pick_translation),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    QuizOptions(
                        options = ex.options,
                        correct = ex.correct,
                        picked = state.lastPicked,
                        revealed = state.revealed,
                        onPick = { option ->
                            vm.answerGame(AnswerEvaluator.textMatches(ex.correct, option, target), option)
                        },
                    )
                }
                is Exercise.OrderChips -> {
                    if (!state.revealed) {
                        OrderExercise(ex, vm)
                    }
                }
                is Exercise.TonePick -> {
                    Text(
                        stringResource(R.string.pick_tone),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    QuizOptions(
                        options = ex.options,
                        correct = ex.correct,
                        picked = state.lastPicked,
                        revealed = state.revealed,
                        onPick = { option ->
                            vm.answerGame(option == ex.correct, option)
                        },
                    )
                }
            }
            if (state.revealed) {
                state.lastCorrect?.let { ok ->
                    val container = if (ok) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                    val onContainer = if (ok) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    }
                    Surface(color = container, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(if (ok) R.string.correct else R.string.incorrect),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = onContainer,
                        )
                    }
                }
                LessonPhraseCard(
                    lang = target,
                    text = concept.text(target).text,
                    ipa = concept.text(target).ipa,
                    speakEnabled = ttsOn,
                    onSpeak = { vm.speak(concept.text(target).text, target) },
                    highlighted = true,
                )
                val hint = concept.text(target).hints[native]
                val tones = concept.text(target).tones
                val grammar = concept.grammar.forLang(native)
                if (!hint.isNullOrBlank() || tones.isNotEmpty() || grammar.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            hint?.takeIf { it.isNotBlank() }?.let {
                                MetaLine(stringResource(R.string.label_hint), it)
                            }
                            if (tones.isNotEmpty()) {
                                MetaLine(stringResource(R.string.label_tones), tones.joinToString())
                            }
                            if (grammar.isNotBlank()) {
                                MetaLine(stringResource(R.string.label_grammar), grammar)
                            }
                        }
                    }
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            stringResource(R.string.third_form),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "${endonym(other)} · ${concept.text(other).text}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                        )
                    }
                }
                if (item.exercise is Exercise.Reveal) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilledTonalButton(
                            onClick = { vm.rate(Rating.Again) },
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                        ) { Text(stringResource(R.string.rating_again)) }
                        FilledTonalButton(
                            onClick = { vm.rate(Rating.Hard) },
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                        ) { Text(stringResource(R.string.rating_hard)) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { vm.rate(Rating.Good) },
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                        ) { Text(stringResource(R.string.rating_good), style = MaterialTheme.typography.titleMedium) }
                        FilledTonalButton(
                            onClick = { vm.rate(Rating.Easy) },
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                        ) { Text(stringResource(R.string.rating_easy)) }
                    }
                } else {
                    Button(
                        onClick = vm::next,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    ) { Text(stringResource(R.string.action_continue), style = MaterialTheme.typography.titleMedium) }
                }
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
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
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
private fun MetaLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
    }
}

@Composable
private fun QuizOptions(
    options: List<String>,
    correct: String,
    picked: String?,
    revealed: Boolean,
    onPick: (String) -> Unit,
) {
    options.forEach { option ->
        val colors = when {
            !revealed -> ButtonDefaults.filledTonalButtonColors()
            option == correct -> ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            option == picked -> ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
            else -> ButtonDefaults.filledTonalButtonColors()
        }
        FilledTonalButton(
            onClick = { onPick(option) },
            enabled = !revealed,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            colors = colors,
        ) {
            Text(option, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderExercise(ex: Exercise.OrderChips, vm: MainViewModel) {
    var built by remember { mutableStateOf(emptyList<String>()) }
    var pool by remember { mutableStateOf(ex.shuffled) }
    Text(
        stringResource(R.string.build_sentence),
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
    Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.onboarding_name)) }, modifier = Modifier.fillMaxWidth().testTag("name"))
        OutlinedTextField(email, { email = it }, label = { Text(stringResource(R.string.onboarding_email)) }, modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.onboarding_language))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.all.forEach { lang ->
                FilterChip(selected = ui == lang, onClick = {
                    ui = lang
                    targets = AppLanguage.all.filter { it != lang }.toSet()
                }, label = { Text(endonym(lang)) })
            }
        }
        Text(stringResource(R.string.onboarding_targets))
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
private fun PrimerScreen(id: String, vm: MainViewModel, onDone: () -> Unit) {
    val title = when (id) {
        "primer_vi" -> R.string.primer_vi_title
        "primer_ru" -> R.string.primer_ru_title
        else -> R.string.primer_en_title
    }
    val body = when (id) {
        "primer_vi" -> R.string.primer_vi_body
        "primer_ru" -> R.string.primer_ru_body
        else -> R.string.primer_en_body
    }
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(body))
        if (id == "primer_vi") {
            Text("ma · má · mà · mả · mã · mạ")
        }
        Button(onClick = { vm.completePrimer(id); onDone() }) { Text(stringResource(R.string.primer_done)) }
    }
}

@Composable
private fun TranslatorScreen(onBack: () -> Unit) {
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.translator_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.translator_stub))
        Button(onClick = onBack) { Text(stringResource(R.string.action_continue)) }
    }
}

@Composable
private fun LanguageRow(selected: AppLanguage, onPick: (AppLanguage) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppLanguage.all.forEach { lang ->
            FilterChip(selected = selected == lang, onClick = { onPick(lang) }, label = { Text(endonym(lang)) })
        }
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
