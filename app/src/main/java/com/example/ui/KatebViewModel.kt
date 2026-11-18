package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.PowerManager
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiPresentationRepository
import com.example.ai.SemanticSearchCandidate
import com.example.ai.SemanticSearchHit
import com.example.ai.SlideDesignSuggestion
import com.example.ai.SmartThemeSuggestion
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.core.localization.stringsFor
import com.example.data.local.CustomFontManager
import com.example.data.local.ImportedFontSpec
import com.example.data.local.KatebDatabase
import com.example.data.local.PresentationEntity
import com.example.data.local.ProjectStorageManager
import com.example.data.local.SettingsDataStore
import com.example.data.local.UserSettings
import com.example.data.remote.CloudAccountUser
import com.example.data.remote.CloudSyncRepository
import com.example.data.repository.PresentationRepository
import com.example.data.templates.PresentationTemplate
import com.example.data.templates.SlideLayoutPreset
import com.example.data.templates.TemplateRepository
import com.example.engine.ChartType
import com.example.engine.MAX_SLIDES_PER_DECK
import com.example.engine.Presentation
import com.example.engine.PresentationSourceType
import com.example.engine.ShapeType
import com.example.engine.SlideAspectRatio
import com.example.engine.SlideBackground
import com.example.engine.SlideElement
import com.example.engine.SlideEngine
import com.example.engine.SlideEngineState
import com.example.engine.SmartArtLayoutType
import com.example.engine.SmartLayoutGeometryService
import com.example.engine.WritingDirection
import com.example.engine.defaultSmartArtNodes
import com.example.engine.defaultTableCells
import com.example.engines.DocumentImportEngine
import com.example.engines.ExportMediaKind
import com.example.engines.Html5ExportEngine
import com.example.engines.ImageExportEngine
import com.example.engines.ImageExportFormat
import com.example.engines.MediaStoreSaver
import com.example.engines.PdfExportEngine
import com.example.engines.PdfPreviewPage
import com.example.engines.PptxEngine
import com.example.engines.VideoExportEngine
import com.example.engines.VideoResolution
import com.example.ui.theme.ThemePreference
import com.example.workers.CloudSyncWorker
import com.example.workers.ExportNotificationWorker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

sealed interface KatebRoute {
    data object Welcome : KatebRoute
    data object Home : KatebRoute
    data object Templates : KatebRoute
    data object Editor : KatebRoute
    data class Present(val startIndex: Int = 0) : KatebRoute
    data object Settings : KatebRoute
    data object Help : KatebRoute
}

sealed interface AsyncOperationState {
    data object Idle : AsyncOperationState
    data class Working(val message: String, val progress: Float? = null) : AsyncOperationState
    data class Success(val message: String, val exportedFile: File? = null) : AsyncOperationState
    data class Error(val message: String) : AsyncOperationState
}

@OptIn(FlowPreview::class)
class KatebViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val db = KatebDatabase.getInstance(context)
    val storage = ProjectStorageManager(context)
    val repository = PresentationRepository(
        context = context,
        dao = db.presentationDao(),
        storage = storage,
        templateDao = db.templateDao(),
        searchHistoryDao = db.searchHistoryDao()
    )
    private val cloudSyncRepository = CloudSyncRepository(context, db.presentationDao(), storage)
    private val settingsStore = SettingsDataStore(context)
    private val fontManager = CustomFontManager(context)
    private val aiRepository = AiPresentationRepository()

    val slideEngine = SlideEngine()
    val engineState: StateFlow<SlideEngineState> = slideEngine.state

    private val _settingsLoaded = MutableStateFlow(false)
    val settingsLoaded: StateFlow<Boolean> = _settingsLoaded.asStateFlow()

    val settings: StateFlow<UserSettings> = settingsStore.settingsFlow
        .onEach { _settingsLoaded.value = true }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserSettings()
        )

    val recentPresentations: StateFlow<List<PresentationEntity>> =
        repository.recentPresentations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPresentations: StateFlow<List<PresentationEntity>> =
        repository.savedPresentations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val importedPresentations: StateFlow<List<PresentationEntity>> =
        repository.importedPresentations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<PresentationTemplate>> =
        repository.observeTemplatesSeeded().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            TemplateRepository.templates
        )

    val recentSearches: StateFlow<List<String>> =
        repository.recentSearches.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cloudUser: StateFlow<CloudAccountUser?> =
        cloudSyncRepository.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val overlapCount: StateFlow<Int> = slideEngine.state
        .map { state ->
            val pres = state.presentation
            val slide = state.activeSlide
            if (pres != null && slide != null) {
                SmartLayoutGeometryService.countOverlaps(slide, pres.canvasWidth, pres.canvasHeight)
            } else 0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _batteryAutoSavePaused = MutableStateFlow(false)
    val batteryAutoSavePaused: StateFlow<Boolean> = _batteryAutoSavePaused.asStateFlow()

    private val _batteryPercent = MutableStateFlow(100)
    val batteryPercent: StateFlow<Int> = _batteryPercent.asStateFlow()

    private val _currentRoute = MutableStateFlow<KatebRoute>(KatebRoute.Home)
    val currentRoute: StateFlow<KatebRoute> = _currentRoute.asStateFlow()

    private val _operationState = MutableStateFlow<AsyncOperationState>(AsyncOperationState.Idle)
    val operationState: StateFlow<AsyncOperationState> = _operationState.asStateFlow()

    private val _pendingRecoveryId = MutableStateFlow<String?>(null)
    val pendingRecoveryId: StateFlow<String?> = _pendingRecoveryId.asStateFlow()

    private val _lastExportedFile = MutableStateFlow<File?>(null)
    val lastExportedFile: StateFlow<File?> = _lastExportedFile.asStateFlow()

    private val _designSuggestionState = MutableStateFlow<SlideDesignSuggestion?>(null)
    val designSuggestionState: StateFlow<SlideDesignSuggestion?> = _designSuggestionState.asStateFlow()

    private val _smartThemeState = MutableStateFlow<SmartThemeSuggestion?>(null)
    val smartThemeState: StateFlow<SmartThemeSuggestion?> = _smartThemeState.asStateFlow()

    private val _deckSummaryState = MutableStateFlow<String?>(null)
    val deckSummaryState: StateFlow<String?> = _deckSummaryState.asStateFlow()

    private val _customFonts = MutableStateFlow<List<ImportedFontSpec>>(emptyList())
    val customFonts: StateFlow<List<ImportedFontSpec>> = _customFonts.asStateFlow()

    private val _semanticResults = MutableStateFlow<List<SemanticSearchHit>>(emptyList())
    val semanticResults: StateFlow<List<SemanticSearchHit>> = _semanticResults.asStateFlow()

    private val _isSemanticSearching = MutableStateFlow(false)
    val isSemanticSearching: StateFlow<Boolean> = _isSemanticSearching.asStateFlow()

    private val _semanticError = MutableStateFlow<String?>(null)
    val semanticError: StateFlow<String?> = _semanticError.asStateFlow()

    private val _pdfPreviewPages = MutableStateFlow<List<PdfPreviewPage>>(emptyList())
    val pdfPreviewPages: StateFlow<List<PdfPreviewPage>> = _pdfPreviewPages.asStateFlow()

    private var _originalPdfPreviewPages: List<PdfPreviewPage> = emptyList()

    init {
        viewModelScope.launch {
            runCatching { repository.ensureTemplatesSeeded() }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val loaded = runCatching { fontManager.loadAndSyncFonts() }.getOrDefault(emptyList())
            _customFonts.value = loaded
        }

        viewModelScope.launch {
            settingsStore.settingsFlow.collect { s ->
                slideEngine.setCanvasLocked(s.editorStabilizeScreen)
                slideEngine.setSnapToGrid(s.editorSnapToGrid)
                slideEngine.setShowGrid(s.editorShowGrid)
                slideEngine.setShowSafeMargins(s.editorShowSafeMargins)
            }
        }

        // Fast local draft autosave whenever presentation changes inside the editor
        viewModelScope.launch {
            slideEngine.state
                .mapNotNull { state -> if (state.isDirty) state.presentation else null }
                .distinctUntilChanged()
                .debounce(1200L)
                .collect { pres ->
                    runCatching { repository.writeAutosave(pres) }
                }
        }

        // Periodic 30-second Room database sync that automatically pauses on low battery / Battery Saver
        viewModelScope.launch {
            while (isActive) {
                refreshBatteryStatus()
                delay(30_000L)
                refreshBatteryStatus()
                if (!_batteryAutoSavePaused.value) {
                    val state = slideEngine.state.value
                    val activePres = state.presentation
                    if (activePres != null && state.isDirty) {
                        runCatching {
                            repository.savePresentation(activePres, clearAutosave = true)
                            slideEngine.markSaved()
                            if (settings.value.cloudSyncEnabled && cloudUser.value != null) {
                                CloudSyncWorker.schedule(context)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun currentStrings(): AppStrings = stringsFor(settings.value.language)

    fun refreshBatteryStatus() {
        val status = readBatterySnapshot()
        _batteryPercent.value = status.first
        _batteryAutoSavePaused.value = settings.value.smartBatterySaverEnabled && status.second
    }

    private fun readBatterySnapshot(): Pair<Int, Boolean> {
        return runCatching {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
            val pct = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt().coerceIn(0, 100) else 100
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val systemPowerSave = powerManager?.isPowerSaveMode == true
            val isLowUnplugged = pct <= 15 && plugged == 0
            pct to (systemPowerSave || isLowUnplugged)
        }.getOrDefault(100 to false)
    }

    fun navigateTo(route: KatebRoute) {
        _currentRoute.value = route
    }

    fun navigateBackFromEditor() {
        val pres = slideEngine.state.value.presentation
        viewModelScope.launch {
            if (pres != null && slideEngine.state.value.isDirty) {
                repository.savePresentation(pres)
                slideEngine.markSaved()
                if (settings.value.cloudSyncEnabled && cloudUser.value != null) {
                    CloudSyncWorker.schedule(context)
                }
            }
            _currentRoute.value = KatebRoute.Home
        }
    }

    fun clearOperationState() {
        _operationState.value = AsyncOperationState.Idle
    }

    fun clearAiInsights() {
        _designSuggestionState.value = null
        _smartThemeState.value = null
        _deckSummaryState.value = null
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsStore.setLanguage(language) }
    }

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch { settingsStore.setThemePreference(theme) }
    }

    fun setDefaultAspectRatio(ratio: SlideAspectRatio) {
        viewModelScope.launch { settingsStore.setDefaultAspectRatio(ratio) }
    }

    fun setSmartBatterySaverEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setSmartBatterySaverEnabled(enabled)
            refreshBatteryStatus()
        }
    }

    fun setBiometricAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setBiometricAppLockEnabled(enabled) }
    }

    fun setCloudSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setCloudSyncEnabled(enabled) }
    }

    fun setCanvasStabilized(enabled: Boolean) {
        slideEngine.setCanvasLocked(enabled)
        viewModelScope.launch { settingsStore.setEditorStabilizeScreen(enabled) }
    }

    fun setSnapToGrid(enabled: Boolean) {
        slideEngine.setSnapToGrid(enabled)
        viewModelScope.launch { settingsStore.setEditorSnapToGrid(enabled) }
    }

    fun setShowGrid(visible: Boolean) {
        slideEngine.setShowGrid(visible)
        viewModelScope.launch { settingsStore.setEditorShowGrid(visible) }
    }

    fun setShowSafeMargins(visible: Boolean) {
        slideEngine.setShowSafeMargins(visible)
        viewModelScope.launch { settingsStore.setEditorShowSafeMargins(visible) }
    }

    fun setCanvasZoom(zoom: Float) {
        slideEngine.setCanvasZoom(zoom)
    }

    fun importCustomFontUri(uri: Uri) {
        val strings = currentStrings()
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) { fontManager.importFontFromUri(uri) }
            res.fold(
                onSuccess = { spec ->
                    _customFonts.value = withContext(Dispatchers.IO) { fontManager.loadAndSyncFonts() }
                    val selected = slideEngine.state.value.selectedElement
                    if (selected is SlideElement.TextElement) {
                        slideEngine.updateElement(selected.copy(fontFamilyId = spec.id), recordUndo = true)
                    }
                    _operationState.value = AsyncOperationState.Success("${strings.fontImportedSuccess}: ${spec.displayName}")
                },
                onFailure = {
                    _operationState.value = AsyncOperationState.Error(strings.invalidFontFile)
                }
            )
        }
    }

    fun deleteCustomFont(fontId: String) {
        viewModelScope.launch {
            val remaining = withContext(Dispatchers.IO) { fontManager.deleteCustomFont(fontId) }
            _customFonts.value = remaining
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsStore.setOnboardingCompleted(true)
            _currentRoute.value = KatebRoute.Home
        }
    }

    fun openWelcomeTour() {
        _currentRoute.value = KatebRoute.Welcome
    }

    fun addSlideWithLayout(preset: SlideLayoutPreset) {
        val pres = slideEngine.state.value.presentation ?: return
        val afterIndex = slideEngine.state.value.selectedSlideIndex
        val newSlide = TemplateRepository.buildSlideFromLayoutPreset(
            preset = preset,
            order = afterIndex + 1,
            presentation = pres
        )
        slideEngine.insertSlide(newSlide, afterIndex)
    }

    fun autoArrangeActiveSlide() {
        slideEngine.autoArrangeActiveSlide()
        _operationState.value = AsyncOperationState.Success(currentStrings().layoutArrangedSuccess)
    }

    fun createBlankPresentation(
        title: String,
        description: String,
        slideCount: Int,
        language: AppLanguage,
        aspectRatio: SlideAspectRatio,
        customWidth: Float = 1920f,
        customHeight: Float = 1080f
    ) {
        val strings = currentStrings()
        viewModelScope.launch {
            val w = if (aspectRatio == SlideAspectRatio.CUSTOM) customWidth.coerceIn(640f, 3840f) else aspectRatio.defaultWidth
            val h = if (aspectRatio == SlideAspectRatio.CUSTOM) customHeight.coerceIn(480f, 3840f) else aspectRatio.defaultHeight
            val slides = (0 until slideCount.coerceIn(1, MAX_SLIDES_PER_DECK)).map { idx ->
                SlideEngine.createDefaultSlide(idx, language, w, h)
            }
            val pres = Presentation(
                id = UUID.randomUUID().toString(),
                title = title.trim().ifEmpty { strings.defaultDeckTitle },
                description = description.trim(),
                language = language,
                aspectRatio = aspectRatio,
                canvasWidth = w,
                canvasHeight = h,
                sourceType = PresentationSourceType.BLANK,
                slides = slides
            )
            repository.savePresentation(pres)
            slideEngine.loadPresentation(pres)
            _currentRoute.value = KatebRoute.Editor
        }
    }

    fun createFromTemplate(
        template: PresentationTemplate,
        title: String,
        description: String,
        slideCount: Int,
        language: AppLanguage,
        aspectRatio: SlideAspectRatio
    ) {
        viewModelScope.launch {
            val pres = TemplateRepository.buildPresentationFromTemplate(
                template = template,
                title = title,
                description = description,
                language = language,
                aspectRatio = aspectRatio,
                slideCount = slideCount
            )
            repository.savePresentation(pres)
            slideEngine.loadPresentation(pres)
            _currentRoute.value = KatebRoute.Editor
        }
    }

    fun applyTemplateThemeToActiveDeck(template: PresentationTemplate) {
        val current = slideEngine.state.value.presentation ?: return
        val themed = TemplateRepository.applyThemeColorsToPresentation(current, template)
        slideEngine.replacePresentation(themed, recordUndo = true)
    }

    fun openPresentation(id: String) {
        viewModelScope.launch {
            val hasDraft = repository.hasAutosaveDraft(id)
            if (hasDraft) {
                _pendingRecoveryId.value = id
            } else {
                val pres = repository.loadPresentation(id, preferAutosave = false) ?: return@launch
                slideEngine.loadPresentation(pres)
                clearAiInsights()
                _currentRoute.value = KatebRoute.Editor
            }
        }
    }

    fun resolveAutosaveRecovery(restore: Boolean) {
        val id = _pendingRecoveryId.value ?: return
        _pendingRecoveryId.value = null
        viewModelScope.launch {
            if (!restore) {
                repository.discardAutosave(id)
            }
            val pres = repository.loadPresentation(id, preferAutosave = restore) ?: return@launch
            slideEngine.loadPresentation(pres)
            clearAiInsights()
            _currentRoute.value = KatebRoute.Editor
        }
    }

    fun saveActivePresentation() {
        val pres = slideEngine.state.value.presentation ?: return
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.savingStatus)
            repository.savePresentation(pres, clearAutosave = true)
            slideEngine.markSaved()
            if (settings.value.cloudSyncEnabled && cloudUser.value != null) {
                CloudSyncWorker.schedule(context)
            }
            _operationState.value = AsyncOperationState.Success(strings.savedStatus)
        }
    }

    fun renamePresentation(id: String, newTitle: String) {
        viewModelScope.launch { repository.renamePresentation(id, newTitle) }
    }

    fun duplicatePresentation(id: String) {
        val strings = currentStrings()
        viewModelScope.launch { repository.duplicatePresentation(id, strings.duplicate) }
    }

    fun deletePresentation(id: String) {
        viewModelScope.launch { repository.deletePresentation(id) }
    }

    fun batchDeletePresentations(ids: List<String>) {
        viewModelScope.launch { repository.batchDeletePresentations(ids) }
    }

    fun batchDuplicatePresentations(ids: List<String>) {
        val strings = currentStrings()
        viewModelScope.launch { repository.batchDuplicatePresentations(ids, strings.duplicate) }
    }

    fun batchMoveToFolder(ids: List<String>, folderTag: String) {
        viewModelScope.launch { repository.batchMoveToFolder(ids, folderTag) }
    }

    fun togglePresentationLock(id: String, locked: Boolean) {
        viewModelScope.launch { repository.setPresentationLocked(id, locked) }
    }

    fun recordSearchQuery(query: String) {
        viewModelScope.launch { repository.recordSearchQuery(query) }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { repository.clearSearchHistory() }
    }

    // ---- Cloud Auth & Sync ----
    fun completeGoogleSignIn(idToken: String) {
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.cloudSyncWorking)
            val signRes = cloudSyncRepository.signInWithGoogleIdToken(idToken)
            signRes.fold(
                onSuccess = {
                    val syncRes = cloudSyncRepository.syncPendingPresentations()
                    _operationState.value = syncRes.fold(
                        onSuccess = { AsyncOperationState.Success(strings.cloudSyncedStatus) },
                        onFailure = { AsyncOperationState.Error(safeErrorMessage(it, strings)) }
                    )
                },
                onFailure = { err ->
                    _operationState.value = AsyncOperationState.Error(safeErrorMessage(err, strings))
                }
            )
        }
    }

    fun signOutCloud() {
        cloudSyncRepository.signOut()
    }

    fun syncCloudNow() {
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.cloudSyncWorking)
            val result = cloudSyncRepository.syncPendingPresentations()
            _operationState.value = result.fold(
                onSuccess = { AsyncOperationState.Success(strings.cloudSyncedStatus) },
                onFailure = { AsyncOperationState.Error(safeErrorMessage(it, strings)) }
            )
        }
    }

    // ---- Element Creation Helpers ----
    private fun defaultFontFor(language: AppLanguage): String = when (language) {
        AppLanguage.AR, AppLanguage.UR -> "cairo"
        AppLanguage.FA -> "vazirmatn"
        AppLanguage.EN, AppLanguage.RU, AppLanguage.IT, AppLanguage.ZH -> "plus_jakarta_sans"
    }

    fun addTextElement(initialText: String? = null) {
        val pres = slideEngine.state.value.presentation ?: return
        val isRtl = pres.language.isRtl
        val defaultFont = defaultFontFor(pres.language)
        val text = initialText ?: when (pres.language) {
            AppLanguage.AR -> "نص جديد - انقر مرتين للتحرير"
            AppLanguage.FA -> "متن جدید - برای ویرایش دوبار بزنید"
            AppLanguage.EN -> "New Text - Double tap to edit"
            AppLanguage.RU -> "Новый текст — нажмите дважды"
            AppLanguage.IT -> "Nuovo testo — tocca due volte"
            AppLanguage.UR -> "نیا متن - ترمیم کے لیے دو بار ٹیپ کریں"
            AppLanguage.ZH -> "新文本 — 双击编辑"
        }
        slideEngine.addElement(
            SlideElement.TextElement(
                x = pres.canvasWidth * 0.15f,
                y = pres.canvasHeight * 0.30f,
                width = pres.canvasWidth * 0.65f,
                height = 180f,
                text = text,
                fontFamilyId = defaultFont,
                fontSizeSp = 34f,
                writingDirection = if (isRtl) WritingDirection.RTL else WritingDirection.LTR
            )
        )
    }

    fun addImageFromUri(uri: Uri) {
        val pres = slideEngine.state.value.presentation ?: return
        viewModelScope.launch {
            val savedPath = storage.importUriToProjectImages(pres.id, uri)
            if (savedPath != null) {
                slideEngine.addElement(
                    SlideElement.ImageElement(
                        x = pres.canvasWidth * 0.25f,
                        y = pres.canvasHeight * 0.20f,
                        width = pres.canvasWidth * 0.45f,
                        height = pres.canvasHeight * 0.50f,
                        localImagePath = savedPath
                    )
                )
            }
        }
    }

    fun setImageBackgroundFromUri(uri: Uri) {
        val pres = slideEngine.state.value.presentation ?: return
        viewModelScope.launch {
            val savedPath = storage.importUriToProjectImages(pres.id, uri)
            if (savedPath != null) {
                slideEngine.updateSlideBackground(SlideBackground.Image(savedPath))
            }
        }
    }

    fun addShapeElement(
        shapeType: ShapeType,
        fillColorHex: String = "#1565C0",
        is3D: Boolean = shapeType.is3DShape,
        depth3D: Float = 24f
    ) {
        val pres = slideEngine.state.value.presentation ?: return
        slideEngine.addElement(
            SlideElement.ShapeElement(
                x = pres.canvasWidth * 0.35f,
                y = pres.canvasHeight * 0.32f,
                width = 360f,
                height = if (shapeType == ShapeType.LINE) 40f else 260f,
                shapeType = shapeType,
                fillColorHex = fillColorHex,
                strokeColorHex = "#0D47A1",
                is3D = is3D || shapeType.is3DShape,
                depth3D = depth3D.coerceIn(8f, 64f)
            )
        )
    }

    fun addSmartArtElement(
        layoutType: SmartArtLayoutType,
        nodeColors: List<String>? = null
    ) {
        val pres = slideEngine.state.value.presentation ?: return
        val baseNodes = defaultSmartArtNodes(layoutType, pres.language)
        val nodes = if (!nodeColors.isNullOrEmpty()) {
            baseNodes.mapIndexed { idx, node ->
                node.copy(fillColorHex = nodeColors[idx % nodeColors.size])
            }
        } else baseNodes
        slideEngine.addElement(
            SlideElement.SmartArtElement(
                x = pres.canvasWidth * 0.08f,
                y = pres.canvasHeight * 0.22f,
                width = pres.canvasWidth * 0.84f,
                height = pres.canvasHeight * 0.62f,
                layoutType = layoutType,
                nodes = nodes,
                fontFamilyId = defaultFontFor(pres.language),
                is3D = true
            )
        )
    }

    fun addTableElement(rows: Int, cols: Int) {
        val pres = slideEngine.state.value.presentation ?: return
        val r = rows.coerceIn(1, 10)
        val c = cols.coerceIn(1, 8)
        slideEngine.addElement(
            SlideElement.TableElement(
                x = pres.canvasWidth * 0.12f,
                y = pres.canvasHeight * 0.24f,
                width = pres.canvasWidth * 0.76f,
                height = (r * 110f).coerceIn(220f, pres.canvasHeight * 0.65f),
                rows = r,
                cols = c,
                cells = defaultTableCells(r, c, pres.language)
            )
        )
    }

    fun addChartElement(chartType: ChartType) {
        val pres = slideEngine.state.value.presentation ?: return
        val chartTitle = when (pres.language) {
            AppLanguage.AR -> "مخطط البيانات"
            AppLanguage.FA -> "نمودار داده‌ها"
            AppLanguage.EN -> "Data Chart"
            AppLanguage.RU -> "Диаграмма данных"
            AppLanguage.IT -> "Grafico dati"
            AppLanguage.UR -> "ڈیٹا چارٹ"
            AppLanguage.ZH -> "数据图表"
        }
        slideEngine.addElement(
            SlideElement.ChartElement(
                x = pres.canvasWidth * 0.15f,
                y = pres.canvasHeight * 0.20f,
                width = pres.canvasWidth * 0.70f,
                height = pres.canvasHeight * 0.62f,
                chartType = chartType,
                title = chartTitle
            )
        )
    }

    fun addEquationElement(expression: String) {
        val pres = slideEngine.state.value.presentation ?: return
        slideEngine.addElement(
            SlideElement.EquationElement(
                x = pres.canvasWidth * 0.20f,
                y = pres.canvasHeight * 0.35f,
                width = pres.canvasWidth * 0.60f,
                height = 180f,
                expression = expression
            )
        )
    }

    fun addIconElement(symbolKey: String) {
        val pres = slideEngine.state.value.presentation ?: return
        slideEngine.addElement(
            SlideElement.IconElement(
                x = pres.canvasWidth * 0.42f,
                y = pres.canvasHeight * 0.36f,
                width = 220f,
                height = 220f,
                symbolKey = symbolKey
            )
        )
    }

    // ---- Imports ----
    fun importPptxUri(uri: Uri) {
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.loadingPptx)
            try {
                val id = UUID.randomUUID().toString()
                val name = resolveFileName(uri).removeSuffix(".pptx").ifBlank { strings.defaultDeckTitle }
                val input = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("file_open_error")
                val pres = input.use {
                    PptxEngine.importFromPptx(it, id, name, storage, settings.value.language)
                }
                repository.savePresentation(pres)
                slideEngine.loadPresentation(pres)
                _operationState.value = AsyncOperationState.Idle
                _currentRoute.value = KatebRoute.Editor
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun importDocumentUri(uri: Uri) {
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.convertingDocument)
            try {
                val id = UUID.randomUUID().toString()
                val rawName = resolveFileName(uri)
                val lower = rawName.lowercase()
                if (lower.endsWith(".pptx")) {
                    _operationState.value = AsyncOperationState.Idle
                    importPptxUri(uri)
                    return@launch
                }
                val title = rawName.substringBeforeLast('.').ifBlank { strings.defaultDeckTitle }
                val lang = settings.value.language

                val pres = when {
                    lower.endsWith(".pdf") -> {
                        DocumentImportEngine.importPdfToPresentation(context, uri, id, title, storage, lang)
                    }
                    lower.endsWith(".docx") -> {
                        val stream = context.contentResolver.openInputStream(uri)
                            ?: throw IllegalStateException("file_open_error")
                        stream.use { DocumentImportEngine.importDocxToPresentation(it, id, title, lang) }
                    }
                    else -> {
                        val stream = context.contentResolver.openInputStream(uri)
                            ?: throw IllegalStateException("file_open_error")
                        stream.use { DocumentImportEngine.importTxtToPresentation(it, id, title, lang) }
                    }
                }
                repository.savePresentation(pres)
                slideEngine.loadPresentation(pres)
                _operationState.value = AsyncOperationState.Idle
                _currentRoute.value = KatebRoute.Editor
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun createFromSelectedImages(uris: List<Uri>, title: String) {
        if (uris.isEmpty()) return
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.buildingPhotoDeck)
            try {
                val id = UUID.randomUUID().toString()
                val pres = DocumentImportEngine.importImagesToPresentation(
                    uris = uris,
                    presentationId = id,
                    title = title.ifBlank { strings.defaultDeckTitle },
                    storage = storage,
                    language = settings.value.language
                )
                repository.savePresentation(pres)
                slideEngine.loadPresentation(pres)
                _operationState.value = AsyncOperationState.Idle
                _currentRoute.value = KatebRoute.Editor
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    // ---- Exports with Real-Time Progress, MediaStore & WorkManager Notifications ----
    fun exportActiveToPptx() {
        val pres = slideEngine.state.value.presentation ?: return
        val strings = currentStrings()
        val lang = settings.value.language
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.exportingPptx, 0.05f)
            try {
                repository.savePresentation(pres)
                val safeName = pres.title.replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_-]"), "_").take(32)
                val outFile = File(storage.exportsPptxDir, "${safeName}_${System.currentTimeMillis()}.pptx")
                PptxEngine.exportToPptx(context, pres, outFile) { p ->
                    _operationState.value = AsyncOperationState.Working(
                        "${strings.exportingPptx} (${(p * 100).toInt()}%)",
                        p
                    )
                }
                MediaStoreSaver.saveFileToPublicCollection(
                    context = context,
                    sourceFile = outFile,
                    displayName = outFile.name,
                    mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    kind = ExportMediaKind.DOCUMENT_OR_PPTX
                )
                ExportNotificationWorker.enqueue(context, true, "PPTX", pres.title, lang)
                _lastExportedFile.value = outFile
                _operationState.value = AsyncOperationState.Success(outFile.name, outFile)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ExportNotificationWorker.enqueue(context, false, "PPTX", pres.title, lang)
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun exportActiveToPdf() {
        val pres = slideEngine.state.value.presentation ?: return
        val strings = currentStrings()
        val lang = settings.value.language
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.renderingPdf, 0.05f)
            try {
                repository.savePresentation(pres)
                val safeName = pres.title.replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_-]"), "_").take(32)
                val outFile = File(storage.exportsPdfDir, "${safeName}_${System.currentTimeMillis()}.pdf")
                PdfExportEngine.exportToPdf(context, pres, outFile) { p ->
                    _operationState.value = AsyncOperationState.Working(
                        "${strings.renderingPdf} (${(p * 100).toInt()}%)",
                        p
                    )
                }
                MediaStoreSaver.saveFileToPublicCollection(
                    context = context,
                    sourceFile = outFile,
                    displayName = outFile.name,
                    mimeType = "application/pdf",
                    kind = ExportMediaKind.DOCUMENT_OR_PPTX
                )
                ExportNotificationWorker.enqueue(context, true, "PDF", pres.title, lang)
                _lastExportedFile.value = outFile
                _operationState.value = AsyncOperationState.Success(outFile.name, outFile)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ExportNotificationWorker.enqueue(context, false, "PDF", pres.title, lang)
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun exportActiveToImages(format: ImageExportFormat) {
        val pres = slideEngine.state.value.presentation ?: return
        val strings = currentStrings()
        val lang = settings.value.language
        val fmtLabel = format.ext.uppercase()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working("${strings.exportingImages} ($fmtLabel)", 0.05f)
            try {
                repository.savePresentation(pres)
                val safeName = pres.title.replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_-]"), "_").take(32)
                val dir = File(storage.exportsImagesDir, "${safeName}_${System.currentTimeMillis()}")
                val files = ImageExportEngine.exportSlidesAsImages(context, pres, dir, format) { p ->
                    _operationState.value = AsyncOperationState.Working(
                        "${strings.exportingImages} ($fmtLabel ${(p * 100).toInt()}%)",
                        p
                    )
                }
                val mime = if (format == ImageExportFormat.PNG) "image/png" else "image/jpeg"
                files.forEach { file ->
                    MediaStoreSaver.saveFileToPublicCollection(
                        context = context,
                        sourceFile = file,
                        displayName = "${safeName}_${file.name}",
                        mimeType = mime,
                        kind = ExportMediaKind.IMAGE
                    )
                }
                ExportNotificationWorker.enqueue(context, true, fmtLabel, pres.title, lang)
                val first = files.firstOrNull()
                _lastExportedFile.value = first
                _operationState.value = AsyncOperationState.Success("${files.size} $fmtLabel", first)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ExportNotificationWorker.enqueue(context, false, fmtLabel, pres.title, lang)
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun exportActiveToMp4(
        resolution: VideoResolution,
        secondsPerSlide: Int,
        audioUri: Uri?
    ) {
        val pres = slideEngine.state.value.presentation ?: return
        val strings = currentStrings()
        val lang = settings.value.language
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working("${strings.encodingMp4} (${resolution.label})", 0.05f)
            try {
                repository.savePresentation(pres)
                val audioPath = audioUri?.let { storage.importUriToProjectAudio(pres.id, it) }
                val safeName = pres.title.replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_-]"), "_").take(32)
                val outFile = File(storage.exportsVideosDir, "${safeName}_${System.currentTimeMillis()}.mp4")
                VideoExportEngine.exportToMp4(
                    context = context,
                    presentation = pres,
                    outputFile = outFile,
                    resolution = resolution,
                    secondsPerSlide = secondsPerSlide,
                    audioFilePath = audioPath,
                    onProgress = { p ->
                        _operationState.value = AsyncOperationState.Working(
                            "${strings.encodingMp4} (${(p * 100).toInt()}%)",
                            p
                        )
                    }
                )
                MediaStoreSaver.saveFileToPublicCollection(
                    context = context,
                    sourceFile = outFile,
                    displayName = outFile.name,
                    mimeType = "video/mp4",
                    kind = ExportMediaKind.VIDEO
                )
                ExportNotificationWorker.enqueue(context, true, "MP4", pres.title, lang)
                _lastExportedFile.value = outFile
                _operationState.value = AsyncOperationState.Success(outFile.name, outFile)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ExportNotificationWorker.enqueue(context, false, "MP4", pres.title, lang)
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun copyFileToSafUri(sourceFile: File, targetUri: Uri) {
        val strings = currentStrings()
        viewModelScope.launch {
            runCatching {
                context.contentResolver.openOutputStream(targetUri)?.use { out ->
                    sourceFile.inputStream().use { inp -> inp.copyTo(out) }
                }
            }.onSuccess {
                _operationState.value = AsyncOperationState.Success(strings.savedToPhoneStorage)
            }.onFailure { err ->
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(err, strings))
            }
        }
    }

    fun generatePdfPreviewPages() {
        val pres = slideEngine.state.value.presentation ?: return
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.renderingPdf, 0.05f)
            try {
                val pages = PdfExportEngine.renderPreviewPages(context, pres) { p ->
                    _operationState.value = AsyncOperationState.Working(
                        "${strings.renderingPdf} (${(p * 100).toInt()}%)",
                        p * 0.9f
                    )
                }
                _originalPdfPreviewPages = pages
                _pdfPreviewPages.value = pages
                _operationState.value = AsyncOperationState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun movePdfPreviewPage(fromIndex: Int, toIndex: Int) {
        val list = _pdfPreviewPages.value.toMutableList()
        if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) return
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        _pdfPreviewPages.value = list
    }

    fun resetPdfPreviewOrder() {
        if (_originalPdfPreviewPages.isNotEmpty()) {
            _pdfPreviewPages.value = _originalPdfPreviewPages
        }
    }

    fun clearPdfPreview() {
        _pdfPreviewPages.value = emptyList()
        _originalPdfPreviewPages = emptyList()
    }

    fun exportPreviewedPdf(applyOrderToDeck: Boolean = false) {
        val pres = slideEngine.state.value.presentation ?: return
        val pages = _pdfPreviewPages.value
        val strings = currentStrings()
        val lang = settings.value.language
        val orderedIds = pages.map { it.slideId }
        val slideById = pres.slides.associateBy { it.id }
        val orderedSlides = if (orderedIds.isNotEmpty()) {
            orderedIds.mapIndexedNotNull { idx, id ->
                slideById[id]?.copy(order = idx)
            }.ifEmpty { pres.slides }
        } else {
            pres.slides
        }
        if (applyOrderToDeck && orderedIds.isNotEmpty()) {
            slideEngine.reorderSlidesByIds(orderedIds)
        }
        val exportPres = pres.copy(slides = orderedSlides)
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.renderingPdf, 0.05f)
            try {
                repository.savePresentation(if (applyOrderToDeck) exportPres else pres)
                val safeName = exportPres.title.replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_-]"), "_").take(32)
                val outFile = File(storage.exportsPdfDir, "${safeName}_${System.currentTimeMillis()}.pdf")
                PdfExportEngine.exportToPdf(context, exportPres, outFile) { p ->
                    _operationState.value = AsyncOperationState.Working(
                        "${strings.renderingPdf} (${(p * 100).toInt()}%)",
                        p
                    )
                }
                MediaStoreSaver.saveFileToPublicCollection(
                    context = context,
                    sourceFile = outFile,
                    displayName = outFile.name,
                    mimeType = "application/pdf",
                    kind = ExportMediaKind.DOCUMENT_OR_PPTX
                )
                ExportNotificationWorker.enqueue(context, true, "PDF", exportPres.title, lang)
                _lastExportedFile.value = outFile
                clearPdfPreview()
                _operationState.value = AsyncOperationState.Success(outFile.name, outFile)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ExportNotificationWorker.enqueue(context, false, "PDF", exportPres.title, lang)
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun exportActiveToHtml5() {
        val pres = slideEngine.state.value.presentation ?: return
        val strings = currentStrings()
        val lang = settings.value.language
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working("${strings.exportHtml5Ios}...", 0.05f)
            try {
                repository.savePresentation(pres)
                val safeName = pres.title.replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_-]"), "_").take(32)
                val webDir = File(storage.rootDir, "Exports/WebHTML").apply { mkdirs() }
                val outFile = File(webDir, "${safeName}_iOS_Safari_${System.currentTimeMillis()}.html")
                Html5ExportEngine.exportToHtml5(context, pres, outFile) { p ->
                    _operationState.value = AsyncOperationState.Working(
                        "${strings.exportHtml5Ios} (${(p * 100).toInt()}%)",
                        p
                    )
                }
                MediaStoreSaver.saveFileToPublicCollection(
                    context = context,
                    sourceFile = outFile,
                    displayName = outFile.name,
                    mimeType = "text/html",
                    kind = ExportMediaKind.DOCUMENT_OR_PPTX
                )
                ExportNotificationWorker.enqueue(context, true, "HTML5", pres.title, lang)
                _lastExportedFile.value = outFile
                _operationState.value = AsyncOperationState.Success(outFile.name, outFile)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ExportNotificationWorker.enqueue(context, false, "HTML5", pres.title, lang)
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun runSemanticSearch(query: String) {
        val clean = query.trim()
        if (clean.isBlank()) {
            clearSemanticSearch()
            return
        }
        val strings = currentStrings()
        val lang = settings.value.language
        val entities = savedPresentations.value
        if (entities.isEmpty()) {
            _semanticResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSemanticSearching.value = true
            _semanticError.value = null
            try {
                repository.recordSearchQuery(clean)
                val candidates = entities.map { entity ->
                    val tpl = TemplateRepository.byIdOrNull(entity.themeId)
                    SemanticSearchCandidate(
                        id = entity.id,
                        title = entity.title,
                        description = entity.description,
                        languageCode = entity.languageCode,
                        themeId = entity.themeId,
                        themeCategory = tpl?.category?.name ?: entity.folderTag,
                        colorPaletteHint = if (tpl != null) "${tpl.primaryHex}, ${tpl.secondaryHex}, ${tpl.accentHex}, ${tpl.bgHex}" else "#1565C0",
                        slideCount = entity.slideCount,
                        sourceType = entity.sourceType
                    )
                }
                _semanticResults.value = aiRepository.semanticSearchPresentations(clean, candidates, lang)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _semanticError.value = safeErrorMessage(e, strings)
            } finally {
                _isSemanticSearching.value = false
            }
        }
    }

    fun clearSemanticSearch() {
        _semanticResults.value = emptyList()
        _semanticError.value = null
        _isSemanticSearching.value = false
    }

    fun buildShareIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val mime = when {
            file.name.endsWith(".pdf", true) -> "application/pdf"
            file.name.endsWith(".pptx", true) -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            file.name.endsWith(".mp4", true) -> "video/mp4"
            file.name.endsWith(".html", true) || file.name.endsWith(".htm", true) -> "text/html"
            file.name.endsWith(".jpg", true) -> "image/jpeg"
            else -> "image/png"
        }
        return Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    // ---- AI Assistant, Real-Time Design Advisor & Smart Theme Engine ----
    fun generatePresentationWithAi(
        prompt: String,
        slideCount: Int,
        language: AppLanguage,
        aspectRatio: SlideAspectRatio
    ) {
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.aiWorking)
            try {
                val pres = aiRepository.generatePresentation(prompt, slideCount, language, aspectRatio)
                repository.savePresentation(pres)
                slideEngine.loadPresentation(pres)
                clearAiInsights()
                _operationState.value = AsyncOperationState.Idle
                _currentRoute.value = KatebRoute.Editor
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun summarizeResearchWithAi(researchText: String, slideCount: Int, language: AppLanguage) {
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.aiWorking)
            try {
                val pres = aiRepository.summarizeResearchToSlides(
                    researchText = researchText,
                    slideCount = slideCount,
                    language = language,
                    aspectRatio = settings.value.defaultAspectRatio
                )
                repository.savePresentation(pres)
                slideEngine.loadPresentation(pres)
                clearAiInsights()
                _operationState.value = AsyncOperationState.Idle
                _currentRoute.value = KatebRoute.Editor
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun translateActivePresentation(targetLanguage: AppLanguage) {
        val current = slideEngine.state.value.presentation ?: return
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.aiWorking)
            try {
                val translated = aiRepository.translatePresentation(current, targetLanguage)
                slideEngine.replacePresentation(translated, recordUndo = true)
                _operationState.value = AsyncOperationState.Success(strings.aiUpdated)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun proofreadSelectedTextElement() {
        val current = slideEngine.state.value.presentation ?: return
        val selected = slideEngine.state.value.selectedElement as? SlideElement.TextElement ?: return
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.aiWorking)
            try {
                val improved = aiRepository.proofreadAndEnhanceText(selected.text, current.language)
                slideEngine.updateElement(selected.copy(text = improved), recordUndo = true)
                _operationState.value = AsyncOperationState.Success(strings.aiUpdated)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun generateSpeakerNotesForActiveSlide() {
        val pres = slideEngine.state.value.presentation ?: return
        val slide = slideEngine.state.value.activeSlide ?: return
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.aiWorking)
            try {
                val notes = aiRepository.generateSpeakerNotes(slide, pres.title, pres.language)
                slideEngine.updateSpeakerNotes(notes)
                _operationState.value = AsyncOperationState.Success(strings.aiUpdated)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun generateAiIllustrationOnSlide(prompt: String) {
        val pres = slideEngine.state.value.presentation ?: return
        val strings = currentStrings()
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.aiWorking)
            try {
                val bmp = aiRepository.generateSlideIllustration(prompt)
                val path = storage.saveBitmapToProjectImages(pres.id, bmp, "ai_art")
                bmp.recycle()
                slideEngine.addElement(
                    SlideElement.ImageElement(
                        x = pres.canvasWidth * 0.25f,
                        y = pres.canvasHeight * 0.22f,
                        width = pres.canvasWidth * 0.48f,
                        height = pres.canvasHeight * 0.50f,
                        localImagePath = path
                    )
                )
                _operationState.value = AsyncOperationState.Success(strings.aiGenerated)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun analyzeActiveSlideDesign() {
        val pres = slideEngine.state.value.presentation ?: return
        val slide = slideEngine.state.value.activeSlide ?: return
        val strings = currentStrings()
        val lang = settings.value.language
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.aiWorking)
            try {
                val suggestion = aiRepository.suggestSlideLayoutDesign(slide, pres.aspectRatio, lang)
                _designSuggestionState.value = suggestion
                _operationState.value = AsyncOperationState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun applySlideDesignSuggestion() {
        val pres = slideEngine.state.value.presentation ?: return
        val slideIdx = slideEngine.state.value.selectedSlideIndex
        val activeSlide = pres.slides.getOrNull(slideIdx) ?: return
        val suggestion = _designSuggestionState.value ?: return
        val updatedSlide = aiRepository.applyDesignSuggestionToSlide(
            slide = activeSlide,
            suggestion = suggestion,
            canvasWidth = pres.canvasWidth,
            canvasHeight = pres.canvasHeight
        )
        val updatedSlides = pres.slides.mapIndexed { idx, s -> if (idx == slideIdx) updatedSlide else s }
        slideEngine.replacePresentation(pres.copy(slides = updatedSlides), recordUndo = true)
        _designSuggestionState.value = null
        _operationState.value = AsyncOperationState.Success(currentStrings().layoutArrangedSuccess)
    }

    fun requestSmartTheme() {
        val pres = slideEngine.state.value.presentation ?: return
        val strings = currentStrings()
        val lang = settings.value.language
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.aiWorking)
            try {
                val suggestion = aiRepository.suggestSmartTheme(pres, lang)
                _smartThemeState.value = suggestion
                _operationState.value = AsyncOperationState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    fun applySmartTheme() {
        val pres = slideEngine.state.value.presentation ?: return
        val theme = _smartThemeState.value ?: return
        val themed = aiRepository.applySmartThemeToPresentation(pres, theme)
        slideEngine.replacePresentation(themed, recordUndo = true)
        _smartThemeState.value = null
        _operationState.value = AsyncOperationState.Success(currentStrings().aiUpdated)
    }

    fun summarizePresentationDeck() {
        val pres = slideEngine.state.value.presentation ?: return
        val strings = currentStrings()
        val lang = settings.value.language
        viewModelScope.launch {
            _operationState.value = AsyncOperationState.Working(strings.aiWorking)
            try {
                val summary = aiRepository.summarizePresentation(pres, lang)
                _deckSummaryState.value = summary
                _operationState.value = AsyncOperationState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _operationState.value = AsyncOperationState.Error(safeErrorMessage(e, strings))
            }
        }
    }

    /**
     * Hides raw exception codes, stack traces, URLs, and internal error IDs from user-facing UI dialogs.
     */
    private fun safeErrorMessage(error: Throwable, strings: AppStrings): String {
        val lang = settings.value.language
        val raw = error.message.orEmpty().lowercase()
        val isTimeoutOrNetwork = raw.contains("timeout") || raw.contains("host") ||
            raw.contains("network") || raw.contains("connect")
        return if (isTimeoutOrNetwork) {
            when (lang) {
                AppLanguage.AR -> "تعذر الاتصال بالخدمة حالياً. يرجى التحقق من الشبكة والمحاولة مرة أخرى."
                AppLanguage.FA -> "اتصال به سرویس در حال حاضر برقرار نشد. لطفاً شبکه خود را بررسی کنید."
                AppLanguage.RU -> "Не удалось подключиться к сервису. Проверьте подключение и повторите."
                AppLanguage.IT -> "Impossibile connettersi al servizio al momento. Controlla la rete."
                AppLanguage.UR -> "سروس سے رابطہ نہیں ہو سکا۔ براہ کرم انٹرنیٹ چیک کریں۔"
                AppLanguage.ZH -> "当前无法连接服务，请检查网络后重试。"
                AppLanguage.EN -> "Unable to reach the service right now. Please check your connection and try again."
            }
        } else {
            when (lang) {
                AppLanguage.AR -> "تعذر إتمام العملية الآن. يرجى المحاولة مرة أخرى."
                AppLanguage.FA -> "انجام این عملیات در حال حاضر ممکن نیست. لطفاً دوباره تلاش کنید."
                AppLanguage.RU -> "Не удалось выполнить операцию. Пожалуйста, повторите попытку."
                AppLanguage.IT -> "Impossibile completare l'operazione ora. Riprova tra poco."
                AppLanguage.UR -> "اس وقت کارروائی مکمل نہیں ہو سکی۔ براہ کرم دوبارہ کوشش کریں۔"
                AppLanguage.ZH -> "当前无法完成此操作，请稍后重试。"
                AppLanguage.EN -> "${strings.errorTitle}: Unable to complete the action right now."
            }
        }
    }

    private fun resolveFileName(uri: Uri): String {
        var result = "document"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) result = cursor.getString(idx) ?: result
            }
        }
        return result
    }
}
