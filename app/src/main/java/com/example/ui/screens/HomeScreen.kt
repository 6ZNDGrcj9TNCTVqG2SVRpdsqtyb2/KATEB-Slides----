package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ai.SemanticSearchHit
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.data.local.PresentationEntity
import com.example.data.remote.CloudAccountUser
import com.example.data.templates.PresentationTemplate
import com.example.data.templates.TemplateRepository
import com.example.engine.parseColorInt
import java.io.File
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    recentDecks: List<PresentationEntity>,
    savedDecks: List<PresentationEntity>,
    importedDecks: List<PresentationEntity>,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onCreateClick: () -> Unit,
    onOpenFileClick: () -> Unit,
    onTemplatesClick: () -> Unit,
    onCreateWithTemplate: (PresentationTemplate) -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onOpenDeck: (String) -> Unit,
    onDuplicateDeck: (String) -> Unit,
    onRenameDeck: (String, String) -> Unit,
    onDeleteDeck: (String) -> Unit,
    recentSearches: List<String> = emptyList(),
    cloudUser: CloudAccountUser? = null,
    onRecordSearch: (String) -> Unit = {},
    onClearSearchHistory: () -> Unit = {},
    onBatchDeleteDecks: (List<String>) -> Unit = {},
    onBatchDuplicateDecks: (List<String>) -> Unit = {},
    onBatchMoveToFolder: (List<String>, String) -> Unit = { _, _ -> },
    onToggleDeckLock: (String, Boolean) -> Unit = { _, _ -> },
    onGoogleSignInClick: () -> Unit = {},
    onSyncNowClick: () -> Unit = {},
    semanticResults: List<SemanticSearchHit> = emptyList(),
    isSemanticSearching: Boolean = false,
    semanticError: String? = null,
    onRunSemanticSearch: (String) -> Unit = {},
    onClearSemanticSearch: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<PresentationEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<PresentationEntity?>(null) }
    var selectedDeckIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    var showMoveFolderDialog by remember { mutableStateOf(false) }
    var selectedFolderFilter by remember { mutableStateOf<String?>(null) }

    val isMultiSelectActive = selectedDeckIds.isNotEmpty()
    BackHandler(enabled = isMultiSelectActive) {
        selectedDeckIds = emptySet()
    }

    val allKnownDecks = remember(savedDecks, recentDecks, importedDecks) {
        (savedDecks + recentDecks + importedDecks).distinctBy { it.id }
    }

    val baseDecks = when (selectedTab) {
        0 -> recentDecks
        1 -> savedDecks
        else -> importedDecks
    }

    val displayedDecks = remember(baseDecks, searchQuery, selectedFolderFilter) {
        val q = searchQuery.trim()
        baseDecks.filter { deck ->
            val matchQuery = q.isEmpty() ||
                deck.title.contains(q, ignoreCase = true) ||
                deck.aspectRatioId.contains(q, ignoreCase = true) ||
                deck.folderTag.contains(q, ignoreCase = true)
            val matchFolder = selectedFolderFilter == null || deck.folderTag == selectedFolderFilter
            matchQuery && matchFolder
        }
    }

    val titleSuggestions = remember(allKnownDecks, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) emptyList()
        else allKnownDecks.filter { it.title.contains(q, ignoreCase = true) }.take(5)
    }

    val folderOptions = remember(strings) {
        listOf(
            "general" to strings.folderGeneral,
            "work" to strings.folderWork,
            "study" to strings.folderStudy,
            "personal" to strings.folderPersonal
        )
    }

    val folderLabelFor: (String) -> String = { key ->
        folderOptions.firstOrNull { it.first == key }?.second ?: strings.folderGeneral
    }

    val featuredTemplates = remember { TemplateRepository.templates.distinctBy { it.category } }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 720.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            floatingActionButton = {
                AnimatedVisibility(visible = !isMultiSelectActive, enter = fadeIn(), exit = fadeOut()) {
                    FloatingActionButton(
                        onClick = onCreateClick,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("home_create_fab")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = strings.createNew)
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWide) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(Modifier.height(12.dp))
                        Image(
                            painter = painterResource(R.drawable.kateb_logo),
                            contentDescription = strings.appName,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.height(16.dp))
                        NavigationRailItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Rounded.History, contentDescription = strings.recentDecks) },
                            label = { Text(strings.recentDecks, maxLines = 1) }
                        )
                        NavigationRailItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Rounded.Layers, contentDescription = strings.savedDecks) },
                            label = { Text(strings.savedDecks, maxLines = 1) }
                        )
                        NavigationRailItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Rounded.FolderOpen, contentDescription = strings.importedDecks) },
                            label = { Text(strings.importedDecks, maxLines = 1) }
                        )
                        NavigationRailItem(
                            selected = false,
                            onClick = onTemplatesClick,
                            icon = { Icon(Icons.Rounded.GridView, contentDescription = strings.templates) },
                            label = { Text(strings.templates, maxLines = 1) }
                        )
                        Spacer(Modifier.weight(1f))
                        NavigationRailItem(
                            selected = false,
                            onClick = onSettingsClick,
                            icon = { Icon(Icons.Rounded.Settings, contentDescription = strings.settings) },
                            label = { Text(strings.settings, maxLines = 1) }
                        )
                        NavigationRailItem(
                            selected = false,
                            onClick = onHelpClick,
                            icon = { Icon(Icons.AutoMirrored.Rounded.HelpOutline, contentDescription = strings.helpAndSupport) },
                            label = { Text(strings.helpAndSupport, maxLines = 1) }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(10.dp))

                    // Contextual Batch Selection Bar when long-pressing decks
                    AnimatedVisibility(
                        visible = isMultiSelectActive,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            tonalElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .testTag("home_multiselect_bar")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { selectedDeckIds = emptySet() },
                                        modifier = Modifier.testTag("batch_close_btn")
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = strings.cancel)
                                    }
                                    Text(
                                        text = "${selectedDeckIds.size} ${strings.selectedCountLabel}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { showMoveFolderDialog = true },
                                        modifier = Modifier.testTag("batch_move_folder_btn")
                                    ) {
                                        Icon(Icons.Rounded.DriveFileMove, contentDescription = strings.moveToFolderAction)
                                    }
                                    IconButton(
                                        onClick = {
                                            onBatchDuplicateDecks(selectedDeckIds.toList())
                                            selectedDeckIds = emptySet()
                                        },
                                        modifier = Modifier.testTag("batch_duplicate_btn")
                                    ) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = strings.duplicate)
                                    }
                                    IconButton(
                                        onClick = { showBatchDeleteConfirm = true },
                                        modifier = Modifier.testTag("batch_delete_btn")
                                    ) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = strings.delete,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Branded Top Bar + Cloud Status + 1-Tap Language Selector + Settings & Help
                    if (!isMultiSelectActive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Image(
                                    painter = painterResource(R.drawable.kateb_logo),
                                    contentDescription = strings.appName,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = strings.appName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = strings.tagline,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (cloudUser != null) onSyncNowClick() else onGoogleSignInClick() },
                                    modifier = Modifier.testTag("home_cloud_btn")
                                ) {
                                    Icon(
                                        imageVector = if (cloudUser != null) Icons.Rounded.CloudDone else Icons.Rounded.CloudUpload,
                                        contentDescription = if (cloudUser != null) strings.syncNowAction else strings.signInGoogle,
                                        tint = if (cloudUser != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box {
                                    FilledTonalButton(
                                        onClick = { showLanguageMenu = true },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("home_lang_btn")
                                    ) {
                                        Icon(Icons.Rounded.Translate, contentDescription = strings.languageSetting, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(currentLanguage.shortCode, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                    DropdownMenu(
                                        expanded = showLanguageMenu,
                                        onDismissRequest = { showLanguageMenu = false }
                                    ) {
                                        AppLanguage.entries.forEach { entry ->
                                            DropdownMenuItem(
                                                text = { Text(entry.nativeName) },
                                                trailingIcon = {
                                                    if (entry == currentLanguage) {
                                                        Icon(Icons.Rounded.Check, contentDescription = null)
                                                    }
                                                },
                                                onClick = {
                                                    showLanguageMenu = false
                                                    onSelectLanguage(entry)
                                                }
                                            )
                                        }
                                    }
                                }

                                if (!isWide) {
                                    IconButton(
                                        onClick = onHelpClick,
                                        modifier = Modifier.testTag("home_help_btn")
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.HelpOutline, contentDescription = strings.helpAndSupport)
                                    }
                                    IconButton(
                                        onClick = onSettingsClick,
                                        modifier = Modifier.testTag("home_settings_btn")
                                    ) {
                                        Icon(Icons.Rounded.Settings, contentDescription = strings.settings)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Hero Action Strip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    )
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = strings.noAccountNotice,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White.copy(alpha = 0.94f)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = onCreateClick,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White,
                                                contentColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("hero_new_btn")
                                        ) {
                                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(strings.createNew, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        FilledTonalButton(
                                            onClick = onTemplatesClick,
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = Color.White.copy(alpha = 0.2f),
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.testTag("hero_templates_btn")
                                        ) {
                                            Icon(Icons.Rounded.GridView, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(strings.templates, maxLines = 1)
                                        }
                                        FilledTonalButton(
                                            onClick = onOpenFileClick,
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = Color.White.copy(alpha = 0.2f),
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.testTag("hero_open_file_btn")
                                        ) {
                                            Icon(Icons.Rounded.FolderOpen, contentDescription = strings.openExisting, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Templates Quick Carousel
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.templates} (${TemplateRepository.templates.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            TextButton(
                                onClick = onTemplatesClick,
                                modifier = Modifier.testTag("home_see_all_templates_btn")
                            ) {
                                Text(strings.seeAll)
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(featuredTemplates, key = { it.id }) { tpl ->
                                HomeTemplateMiniCard(
                                    template = tpl,
                                    title = tpl.name(lang),
                                    categoryLabel = tpl.category.label(lang),
                                    onClick = { onCreateWithTemplate(tpl) }
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }

                    // Search Saved Presentations + AI Semantic Search + Live Suggestions Dropdown
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                if (it.isBlank() && semanticResults.isNotEmpty()) {
                                    onClearSemanticSearch()
                                }
                            },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = strings.searchDecksHint, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        onClearSemanticSearch()
                                    }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = strings.cancel, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            placeholder = { Text(strings.semanticSearchHint, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    onRecordSearch(searchQuery)
                                    onRunSemanticSearch(searchQuery)
                                    searchFocused = false
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { searchFocused = it.isFocused }
                                .testTag("home_search_input")
                        )

                        FilledTonalButton(
                            onClick = {
                                val effective = searchQuery.ifBlank { "Academic Royal Blue" }
                                onRunSemanticSearch(effective)
                            },
                            enabled = !isSemanticSearching && allKnownDecks.isNotEmpty(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                            modifier = Modifier.testTag("home_semantic_search_btn")
                        ) {
                            if (isSemanticSearching) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = strings.semanticSearchAction, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(strings.semanticSearchAction, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                    }

                    if (semanticError != null) {
                        Text(
                            text = semanticError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = semanticResults.isNotEmpty(),
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .testTag("home_semantic_results")
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = strings.semanticSearchResultsTitle,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    TextButton(
                                        onClick = onClearSemanticSearch,
                                        modifier = Modifier.testTag("home_semantic_clear_btn")
                                    ) {
                                        Text(strings.clearSemanticSearch, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                semanticResults.forEach { hit ->
                                    val matchedDeck = allKnownDecks.firstOrNull { it.id == hit.presentationId }
                                    if (matchedDeck != null) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            onClick = { onOpenDeck(matchedDeck.id) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("semantic_hit_${matchedDeck.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(matchedDeck.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                                    if (hit.reason.isNotBlank()) {
                                                        Text(
                                                            hit.reason,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        "${hit.score}%",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val showSuggestionPanel = searchQuery.isNotBlank() && titleSuggestions.isNotEmpty() ||
                        (searchFocused && searchQuery.isBlank() && recentSearches.isNotEmpty())

                    AnimatedVisibility(
                        visible = showSuggestionPanel,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            tonalElevation = 4.dp,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                                .testTag("home_search_suggestions")
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (titleSuggestions.isNotEmpty()) {
                                    Text(
                                        text = strings.suggestionsLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    titleSuggestions.forEach { match ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    searchQuery = match.title
                                                    onRecordSearch(match.title)
                                                    onOpenDeck(match.id)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                                .testTag("search_suggestion_${match.id}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (match.isLocked) Icons.Rounded.Lock else Icons.Rounded.Slideshow,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = match.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = "${match.slideCount} ${strings.slidesCountLabel}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (recentSearches.isNotEmpty()) {
                                    if (titleSuggestions.isNotEmpty()) HorizontalDivider()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = strings.recentSearchesLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                        TextButton(
                                            onClick = onClearSearchHistory,
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                        ) {
                                            Text(strings.clearHistoryAction, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        recentSearches.take(6).forEach { term ->
                                            InputChip(
                                                selected = false,
                                                onClick = { searchQuery = term },
                                                label = { Text(term, style = MaterialTheme.typography.labelSmall) },
                                                leadingIcon = {
                                                    Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(14.dp))
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                label = { Text("${strings.recentDecks} (${recentDecks.size})") },
                                modifier = Modifier.testTag("tab_recent")
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                label = { Text("${strings.savedDecks} (${savedDecks.size})") },
                                modifier = Modifier.testTag("tab_saved")
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                label = { Text("${strings.importedDecks} (${importedDecks.size})") },
                                modifier = Modifier.testTag("tab_imported")
                            )
                        }
                        items(folderOptions, key = { it.first }) { (tagKey, tagLabel) ->
                            FilterChip(
                                selected = selectedFolderFilter == tagKey,
                                onClick = {
                                    selectedFolderFilter = if (selectedFolderFilter == tagKey) null else tagKey
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Folder, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                label = { Text(tagLabel) }
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    if (displayedDecks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.Slideshow,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = strings.emptyDecksTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.emptyDecksHint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(onClick = onCreateClick) {
                                    Icon(Icons.Rounded.Add, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.createNew)
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 270.dp),
                            contentPadding = PaddingValues(bottom = 88.dp, top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(displayedDecks, key = { it.id }) { deck ->
                                val isSelected = deck.id in selectedDeckIds
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                    modifier = Modifier.animateItem()
                                ) {
                                    PresentationCard(
                                        entity = deck,
                                        folderDisplayName = folderLabelFor(deck.folderTag),
                                        isSelected = isSelected,
                                        inSelectionMode = isMultiSelectActive,
                                        onClick = {
                                            if (isMultiSelectActive) {
                                                selectedDeckIds = if (isSelected) selectedDeckIds - deck.id else selectedDeckIds + deck.id
                                            } else {
                                                if (searchQuery.isNotBlank()) onRecordSearch(searchQuery)
                                                onOpenDeck(deck.id)
                                            }
                                        },
                                        onLongClick = {
                                            selectedDeckIds = if (isSelected) selectedDeckIds - deck.id else selectedDeckIds + deck.id
                                        },
                                        onDuplicate = { onDuplicateDeck(deck.id) },
                                        onRename = {
                                            renameText = deck.title
                                            renameTarget = deck
                                        },
                                        onToggleLock = {
                                            onToggleDeckLock(deck.id, !deck.isLocked)
                                        },
                                        onDelete = { deleteTarget = deck }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (renameTarget != null) {
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(strings.rename) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text(strings.deckTitleLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    renameTarget?.let { onRenameDeck(it.id, renameText) }
                    renameTarget = null
                }) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text(strings.cancel) }
            }
        )
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(strings.delete) },
            text = { Text(deleteTarget?.title.orEmpty()) },
            confirmButton = {
                Button(
                    onClick = {
                        deleteTarget?.let { onDeleteDeck(it.id) }
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text(strings.cancel) }
            }
        )
    }

    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text(strings.delete) },
            text = { Text("${selectedDeckIds.size} ${strings.selectedCountLabel}") },
            confirmButton = {
                Button(
                    onClick = {
                        onBatchDeleteDecks(selectedDeckIds.toList())
                        selectedDeckIds = emptySet()
                        showBatchDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirm = false }) { Text(strings.cancel) }
            }
        )
    }

    if (showMoveFolderDialog) {
        AlertDialog(
            onDismissRequest = { showMoveFolderDialog = false },
            title = { Text(strings.moveToFolderAction) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    folderOptions.forEach { (key, label) ->
                        OutlinedButton(
                            onClick = {
                                onBatchMoveToFolder(selectedDeckIds.toList(), key)
                                selectedDeckIds = emptySet()
                                showMoveFolderDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMoveFolderDialog = false }) { Text(strings.cancel) }
            }
        )
    }
}

@Composable
private fun HomeTemplateMiniCard(
    template: PresentationTemplate,
    title: String,
    categoryLabel: String,
    onClick: () -> Unit
) {
    val primary = Color(parseColorInt(template.primaryHex))
    val secondary = Color(parseColorInt(template.secondaryHex))
    val accent = Color(parseColorInt(template.accentHex))

    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .width(156.dp)
            .clickable(onClick = onClick)
            .testTag("home_tpl_${template.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .background(Brush.linearGradient(listOf(primary, secondary)))
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accent)
                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            )
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = categoryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresentationCard(
    entity: PresentationEntity,
    folderDisplayName: String,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onToggleLock: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    val formattedDate = remember(entity.updatedAt) {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(entity.updatedAt))
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 5.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
                else Modifier
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("deck_card_${entity.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val thumbFile = File(entity.thumbnailPath)
                if (thumbFile.exists()) {
                    AsyncImage(
                        model = thumbFile,
                        contentDescription = entity.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Slideshow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                // Folder tag & Biometric Lock badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.62f)
                    ) {
                        Text(
                            text = folderDisplayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                    if (entity.isLocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(Icons.Rounded.Lock, contentDescription = strings.deckLockedBadge, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(strings.deckLockedBadge, style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }

                if (inSelectionMode) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onClick() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("deck_checkbox_${entity.id}")
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${entity.slideCount} ${strings.slidesCountLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = entity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$formattedDate • ${entity.aspectRatioId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("deck_open_${entity.id}")
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.open)
                    }
                    Row {
                        IconButton(onClick = onToggleLock, modifier = Modifier.testTag("deck_lock_${entity.id}")) {
                            Icon(
                                imageVector = if (entity.isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                contentDescription = if (entity.isLocked) strings.unlockDeckAction else strings.lockDeckAction,
                                tint = if (entity.isLocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        IconButton(onClick = onDuplicate, modifier = Modifier.testTag("deck_duplicate_${entity.id}")) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = strings.duplicate, modifier = Modifier.size(19.dp))
                        }
                        IconButton(onClick = onRename, modifier = Modifier.testTag("deck_rename_${entity.id}")) {
                            Icon(Icons.Rounded.DriveFileRenameOutline, contentDescription = strings.rename, modifier = Modifier.size(19.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.testTag("deck_delete_${entity.id}")) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = strings.delete,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
