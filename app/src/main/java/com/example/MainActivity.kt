package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.firebase.configureAppCheck
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.stringsFor
import com.example.core.security.BiometricAuthManager
import com.example.engine.MAX_SLIDES_PER_DECK
import com.example.ui.AsyncOperationState
import com.example.ui.KatebRoute
import com.example.ui.KatebViewModel
import com.example.ui.editor.EditorScreen
import com.example.ui.screens.CreatePresentationSheet
import com.example.ui.screens.HelpScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PresentationPlayerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TemplatesScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.KatebSlidesTheme
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase & App Check with the launching Intent before ViewModel / AI usage
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            KatebSlidesApp()
        }
    }
}

@Composable
fun KatebSlidesApp() {
    val viewModel: KatebViewModel = viewModel()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val coroutineScope = rememberCoroutineScope()

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val settingsLoaded by viewModel.settingsLoaded.collectAsStateWithLifecycle()
    val route by viewModel.currentRoute.collectAsStateWithLifecycle()
    val engineState by viewModel.engineState.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()
    val pendingRecoveryId by viewModel.pendingRecoveryId.collectAsStateWithLifecycle()
    val recentDecks by viewModel.recentPresentations.collectAsStateWithLifecycle()
    val savedDecks by viewModel.savedPresentations.collectAsStateWithLifecycle()
    val importedDecks by viewModel.importedPresentations.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val cloudUser by viewModel.cloudUser.collectAsStateWithLifecycle()
    val batteryPercent by viewModel.batteryPercent.collectAsStateWithLifecycle()
    val batteryAutoSavePaused by viewModel.batteryAutoSavePaused.collectAsStateWithLifecycle()
    val semanticResults by viewModel.semanticResults.collectAsStateWithLifecycle()
    val isSemanticSearching by viewModel.isSemanticSearching.collectAsStateWithLifecycle()
    val semanticError by viewModel.semanticError.collectAsStateWithLifecycle()

    var showCreateSheet by remember { mutableStateOf(false) }
    var templatesApplyToActiveDeck by remember { mutableStateOf(false) }
    var pendingPhotoDeckTitle by remember { mutableStateOf("") }
    var pendingVideoAudioUri by remember { mutableStateOf<Uri?>(null) }
    var filePendingSafSave by remember { mutableStateOf<File?>(null) }
    var pendingExportAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    var isAppUnlocked by remember { mutableStateOf(false) }
    var biometricLockError by remember { mutableStateOf<String?>(null) }

    val strings = remember(settings.language) { stringsFor(settings.language) }

    val requiresStartupUnlock = settingsLoaded &&
        settings.biometricAppLockEnabled &&
        !isAppUnlocked &&
        BiometricAuthManager.canAuthenticate(context)

    LaunchedEffect(requiresStartupUnlock) {
        if (requiresStartupUnlock && activity != null) {
            BiometricAuthManager.authenticate(
                activity = activity,
                title = strings.unlockAppTitle,
                subtitle = strings.unlockAppSubtitle,
                onSuccess = {
                    isAppUnlocked = true
                    biometricLockError = null
                },
                onError = { err ->
                    biometricLockError = err.ifBlank { strings.biometricNotAvailable }
                }
            )
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        val action = pendingExportAction
        pendingExportAction = null
        action?.invoke()
    }

    val runWithNotificationPermission: (() -> Unit) -> Unit = { exportAction ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingExportAction = exportAction
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            exportAction()
        }
    }

    val launchGoogleSignIn: () -> Unit = {
        coroutineScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val option = GetSignInWithGoogleOption.Builder(
                    context.getString(R.string.default_web_client_id)
                ).build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(option)
                    .build()
                val response = credentialManager.getCredential(context, request)
                val googleCred = GoogleIdTokenCredential.createFrom(response.credential.data)
                viewModel.completeGoogleSignIn(googleCred.idToken)
            } catch (_: GetCredentialCancellationException) {
                // User cancelled gracefully
            } catch (_: GetCredentialException) {
                biometricLockError = strings.cloudSignInCancelled
            } catch (_: Exception) {
                biometricLockError = strings.cloudSignInCancelled
            }
        }
    }

    // Activity Result Launchers (Zero-permission Photo Picker + SAF Document Pickers)
    val pickSlideImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.addImageFromUri(uri)
    }

    val pickBackgroundImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.setImageBackgroundFromUri(uri)
    }

    val pickMultiplePhotosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = MAX_SLIDES_PER_DECK)
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.createFromSelectedImages(uris, pendingPhotoDeckTitle)
    }

    val pickPptxLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.importPptxUri(uri)
    }

    val pickDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.importDocumentUri(uri)
    }

    val pickAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) pendingVideoAudioUri = uri
    }

    val pickFontLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.importCustomFontUri(uri)
    }

    val saveExportSafLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { targetUri ->
        val src = filePendingSafSave
        filePendingSafSave = null
        if (targetUri != null && src != null && src.exists()) {
            viewModel.copyFileToSafUri(src, targetUri)
        }
    }

    CompositionLocalProvider(
        LocalAppLanguage provides settings.language,
        LocalAppStrings provides strings,
        LocalLayoutDirection provides settings.language.layoutDirection
    ) {
        KatebSlidesTheme(themePreference = settings.themePreference) {
            Box(modifier = Modifier.fillMaxSize()) {
                val effectiveRoute = if (route == KatebRoute.Home && settingsLoaded && !settings.hasCompletedOnboarding) {
                    KatebRoute.Welcome
                } else {
                    route
                }

                LaunchedEffect(effectiveRoute) {
                    if (effectiveRoute != KatebRoute.Templates && templatesApplyToActiveDeck) {
                        templatesApplyToActiveDeck = false
                    }
                }

                AnimatedContent(
                    targetState = effectiveRoute,
                    contentKey = { it::class },
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 18 }) togetherWith
                            fadeOut(tween(170))
                    },
                    label = "app_route_transition"
                ) { current ->
                    when (current) {
                    KatebRoute.Welcome -> {
                        WelcomeScreen(
                            currentLanguage = settings.language,
                            onSelectLanguage = { viewModel.setLanguage(it) },
                            onFinish = { viewModel.completeOnboarding() }
                        )
                    }

                    KatebRoute.Home -> {
                        HomeScreen(
                            recentDecks = recentDecks,
                            savedDecks = savedDecks,
                            importedDecks = importedDecks,
                            currentLanguage = settings.language,
                            onSelectLanguage = { viewModel.setLanguage(it) },
                            onCreateClick = { showCreateSheet = true },
                            onOpenFileClick = {
                                pickDocumentLauncher.launch(
                                    arrayOf(
                                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                        "application/pdf",
                                        "text/plain"
                                    )
                                )
                            },
                            onTemplatesClick = {
                                templatesApplyToActiveDeck = false
                                viewModel.navigateTo(KatebRoute.Templates)
                            },
                            onCreateWithTemplate = { tpl ->
                                viewModel.createFromTemplate(
                                    template = tpl,
                                    title = tpl.name(settings.language),
                                    description = "",
                                    slideCount = 5,
                                    language = settings.language,
                                    aspectRatio = settings.defaultAspectRatio
                                )
                            },
                            onSettingsClick = { viewModel.navigateTo(KatebRoute.Settings) },
                            onHelpClick = { viewModel.navigateTo(KatebRoute.Help) },
                            onOpenDeck = { id ->
                                val allDecks = savedDecks + recentDecks + importedDecks
                                val target = allDecks.firstOrNull { it.id == id }
                                if (target?.isLocked == true && activity != null && BiometricAuthManager.canAuthenticate(activity)) {
                                    BiometricAuthManager.authenticate(
                                        activity = activity,
                                        title = strings.biometricAuthPromptTitle,
                                        subtitle = strings.biometricAuthPromptSubtitle,
                                        onSuccess = { viewModel.openPresentation(id) },
                                        onError = { err -> biometricLockError = err.ifBlank { strings.biometricNotAvailable } }
                                    )
                                } else {
                                    viewModel.openPresentation(id)
                                }
                            },
                            onDuplicateDeck = { id -> viewModel.duplicatePresentation(id) },
                            onRenameDeck = { id, newTitle -> viewModel.renamePresentation(id, newTitle) },
                            onDeleteDeck = { id -> viewModel.deletePresentation(id) },
                            recentSearches = recentSearches,
                            cloudUser = cloudUser,
                            onRecordSearch = { query -> viewModel.recordSearchQuery(query) },
                            onClearSearchHistory = { viewModel.clearSearchHistory() },
                            onBatchDeleteDecks = { ids -> viewModel.batchDeletePresentations(ids) },
                            onBatchDuplicateDecks = { ids -> viewModel.batchDuplicatePresentations(ids) },
                            onBatchMoveToFolder = { ids, folder -> viewModel.batchMoveToFolder(ids, folder) },
                            onToggleDeckLock = { id, locked ->
                                if (!locked) {
                                    if (activity != null && BiometricAuthManager.canAuthenticate(activity)) {
                                        BiometricAuthManager.authenticate(
                                            activity = activity,
                                            title = strings.unlockDeckAction,
                                            subtitle = strings.biometricAuthPromptSubtitle,
                                            onSuccess = { viewModel.togglePresentationLock(id, false) },
                                            onError = { err -> biometricLockError = err }
                                        )
                                    } else {
                                        viewModel.togglePresentationLock(id, false)
                                    }
                                } else {
                                    viewModel.togglePresentationLock(id, true)
                                }
                            },
                            onGoogleSignInClick = launchGoogleSignIn,
                            onSyncNowClick = { viewModel.syncCloudNow() },
                            semanticResults = semanticResults,
                            isSemanticSearching = isSemanticSearching,
                            semanticError = semanticError,
                            onRunSemanticSearch = { q -> viewModel.runSemanticSearch(q) },
                            onClearSemanticSearch = { viewModel.clearSemanticSearch() }
                        )
                    }

                    KatebRoute.Templates -> {
                        BackHandler {
                            viewModel.navigateTo(
                                if (templatesApplyToActiveDeck) KatebRoute.Editor else KatebRoute.Home
                            )
                        }
                        TemplatesScreen(
                            templates = templates,
                            isApplyToExistingDeck = templatesApplyToActiveDeck,
                            onBack = {
                                viewModel.navigateTo(
                                    if (templatesApplyToActiveDeck) KatebRoute.Editor else KatebRoute.Home
                                )
                            },
                            onSelectTemplate = { tpl ->
                                if (templatesApplyToActiveDeck) {
                                    viewModel.applyTemplateThemeToActiveDeck(tpl)
                                    viewModel.navigateTo(KatebRoute.Editor)
                                } else {
                                    viewModel.createFromTemplate(
                                        template = tpl,
                                        title = tpl.name(settings.language),
                                        description = "",
                                        slideCount = 5,
                                        language = settings.language,
                                        aspectRatio = settings.defaultAspectRatio
                                    )
                                }
                            }
                        )
                    }

                    KatebRoute.Editor -> {
                        EditorScreen(
                            viewModel = viewModel,
                            engineState = engineState,
                            hasAttachedAudio = pendingVideoAudioUri != null,
                            onBack = { viewModel.navigateBackFromEditor() },
                            onPresent = { startIndex -> viewModel.navigateTo(KatebRoute.Present(startIndex)) },
                            onOpenDesignTemplates = {
                                templatesApplyToActiveDeck = true
                                viewModel.navigateTo(KatebRoute.Templates)
                            },
                            onPickImageForSlide = {
                                pickSlideImageLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onPickImageForBackground = {
                                pickBackgroundImageLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onPickVideoAudio = {
                                pickAudioLauncher.launch(arrayOf("audio/*"))
                            },
                            onPickCustomFont = {
                                pickFontLauncher.launch(
                                    arrayOf(
                                        "font/ttf",
                                        "font/otf",
                                        "application/x-font-ttf",
                                        "application/x-font-opentype",
                                        "application/octet-stream",
                                        "*/*"
                                    )
                                )
                            },
                            onExportPptx = {
                                runWithNotificationPermission { viewModel.exportActiveToPptx() }
                            },
                            onExportPdf = {
                                runWithNotificationPermission { viewModel.exportActiveToPdf() }
                            },
                            onExportImages = { fmt ->
                                runWithNotificationPermission { viewModel.exportActiveToImages(fmt) }
                            },
                            onExportMp4 = { res, secPerSlide ->
                                runWithNotificationPermission {
                                    viewModel.exportActiveToMp4(res, secPerSlide, pendingVideoAudioUri)
                                }
                            },
                            onExportHtml5Ios = {
                                runWithNotificationPermission { viewModel.exportActiveToHtml5() }
                            }
                        )
                    }

                    is KatebRoute.Present -> {
                        val activePres = engineState.presentation
                        if (activePres != null) {
                            PresentationPlayerScreen(
                                presentation = activePres,
                                initialSlideIndex = current.startIndex,
                                onExit = { viewModel.navigateTo(KatebRoute.Editor) }
                            )
                        } else {
                            LaunchedEffect(Unit) { viewModel.navigateTo(KatebRoute.Home) }
                        }
                    }

                    KatebRoute.Settings -> {
                        SettingsScreen(
                            settings = settings,
                            storagePath = viewModel.storage.rootDir.absolutePath,
                            onSelectLanguage = { viewModel.setLanguage(it) },
                            onSelectTheme = { viewModel.setThemePreference(it) },
                            onSelectAspectRatio = { viewModel.setDefaultAspectRatio(it) },
                            onOpenWelcome = { viewModel.openWelcomeTour() },
                            onBack = { viewModel.navigateTo(KatebRoute.Home) },
                            batteryPercent = batteryPercent,
                            batteryAutoSavePaused = batteryAutoSavePaused,
                            cloudUser = cloudUser,
                            onToggleBatterySaver = { viewModel.setSmartBatterySaverEnabled(it) },
                            onToggleBiometricLock = { enable ->
                                if (enable && activity != null && BiometricAuthManager.canAuthenticate(activity)) {
                                    BiometricAuthManager.authenticate(
                                        activity = activity,
                                        title = strings.biometricLockSetting,
                                        subtitle = strings.biometricAuthPromptSubtitle,
                                        onSuccess = {
                                            isAppUnlocked = true
                                            viewModel.setBiometricAppLockEnabled(true)
                                        },
                                        onError = { err -> biometricLockError = err.ifBlank { strings.biometricNotAvailable } }
                                    )
                                } else {
                                    viewModel.setBiometricAppLockEnabled(enable)
                                }
                            },
                            onToggleCloudSync = { viewModel.setCloudSyncEnabled(it) },
                            onGoogleSignIn = launchGoogleSignIn,
                            onSignOutCloud = { viewModel.signOutCloud() },
                            onSyncCloudNow = { viewModel.syncCloudNow() },
                            onToggleStabilizeScreen = { viewModel.setCanvasStabilized(it) },
                            onToggleSnapToGrid = { viewModel.setSnapToGrid(it) }
                        )
                    }

                    KatebRoute.Help -> {
                        HelpScreen(
                            onOpenWelcome = { viewModel.openWelcomeTour() },
                            onBack = { viewModel.navigateTo(KatebRoute.Home) }
                        )
                    }
                    }
                }

                // Create Presentation Bottom Sheet
                if (showCreateSheet) {
                    CreatePresentationSheet(
                        defaultAspectRatio = settings.defaultAspectRatio,
                        onDismiss = { showCreateSheet = false },
                        onCreateBlank = { title, desc, count, lang, ratio, customW, customH ->
                            viewModel.createBlankPresentation(title, desc, count, lang, ratio, customW, customH)
                        },
                        onCreateFromTemplate = { tpl, title, desc, count, lang, ratio ->
                            viewModel.createFromTemplate(tpl, title, desc, count, lang, ratio)
                        },
                        onGenerateWithAi = { prompt, count, lang, ratio ->
                            viewModel.generatePresentationWithAi(prompt, count, lang, ratio)
                        },
                        onPickPptx = {
                            pickPptxLauncher.launch(
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                    "*/*"
                                )
                            )
                        },
                        onPickDocxOrTxt = {
                            pickDocumentLauncher.launch(
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "text/plain"
                                )
                            )
                        },
                        onPickPdf = {
                            pickDocumentLauncher.launch(arrayOf("application/pdf"))
                        },
                        onPickPhotos = { title ->
                            pendingPhotoDeckTitle = title
                            pickMultiplePhotosLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }

                // Startup Biometric App Lock Overlay
                if (requiresStartupUnlock) {
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("biometric_lock_overlay")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(R.drawable.kateb_logo),
                                contentDescription = strings.appName,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(18.dp))
                            )
                            Spacer(Modifier.height(20.dp))
                            Icon(
                                Icons.Rounded.Fingerprint,
                                contentDescription = strings.biometricLockSetting,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = strings.unlockAppTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = strings.unlockAppSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    if (activity != null) {
                                        BiometricAuthManager.authenticate(
                                            activity = activity,
                                            title = strings.unlockAppTitle,
                                            subtitle = strings.unlockAppSubtitle,
                                            onSuccess = {
                                                isAppUnlocked = true
                                                biometricLockError = null
                                            },
                                            onError = { err ->
                                                biometricLockError = err.ifBlank { strings.biometricNotAvailable }
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("biometric_unlock_btn")
                            ) {
                                Icon(Icons.Rounded.Fingerprint, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(strings.unlockAction)
                            }
                        }
                    }
                }

                // Autosave Crash Recovery Dialog
                if (pendingRecoveryId != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.resolveAutosaveRecovery(restore = false) },
                        title = { Text(strings.restoreAutosaveTitle) },
                        text = { Text(strings.restoreAutosaveMessage) },
                        confirmButton = {
                            Button(onClick = { viewModel.resolveAutosaveRecovery(restore = true) }) {
                                Text(strings.restoreAction)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.resolveAutosaveRecovery(restore = false) }) {
                                Text(strings.discardAction)
                            }
                        }
                    )
                }

                // Sanitized Biometric / Auth Alert
                if (biometricLockError != null) {
                    AlertDialog(
                        onDismissRequest = { biometricLockError = null },
                        title = { Text(strings.errorTitle) },
                        text = { Text(biometricLockError.orEmpty()) },
                        confirmButton = {
                            Button(onClick = { biometricLockError = null }) {
                                Text(strings.done)
                            }
                        }
                    )
                }

                // Global Async Operation Overlay (AI / Export / Import)
                when (val op = operationState) {
                    is AsyncOperationState.Working -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                                .testTag("operation_loading_overlay")
                        ) {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                tonalElevation = 6.dp,
                                modifier = Modifier
                                    .padding(28.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    if (op.progress != null) {
                                        LinearProgressIndicator(
                                            progress = { op.progress.coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("export_progress_bar")
                                        )
                                    } else {
                                        CircularProgressIndicator()
                                    }
                                    Text(
                                        text = op.message,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    is AsyncOperationState.Success -> {
                        val exportedFile = op.exportedFile
                        if (exportedFile != null) {
                            AlertDialog(
                                onDismissRequest = { viewModel.clearOperationState() },
                                icon = {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                },
                                title = { Text(strings.exportSuccess) },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(op.message, fontWeight = FontWeight.Medium)
                                        Text(
                                            text = strings.savedToPhoneStorage,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                confirmButton = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = {
                                            filePendingSafSave = exportedFile
                                            saveExportSafLauncher.launch(exportedFile.name)
                                            viewModel.clearOperationState()
                                        }) {
                                            Icon(Icons.Rounded.FolderCopy, null)
                                            Spacer(Modifier.width(4.dp))
                                            Text(strings.saveToPhoneFolder)
                                        }
                                        Button(onClick = {
                                            val shareIntent = viewModel.buildShareIntent(exportedFile)
                                            context.startActivity(Intent.createChooser(shareIntent, strings.share))
                                            viewModel.clearOperationState()
                                        }) {
                                            Icon(Icons.Rounded.Share, null)
                                            Spacer(Modifier.width(4.dp))
                                            Text(strings.share)
                                        }
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { viewModel.clearOperationState() }) {
                                        Text(strings.done)
                                    }
                                }
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 48.dp, start = 20.dp, end = 20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(op.message, modifier = Modifier.weight(1f, fill = false))
                                    TextButton(onClick = { viewModel.clearOperationState() }) {
                                        Text(strings.done)
                                    }
                                }
                            }
                        }
                    }

                    is AsyncOperationState.Error -> {
                        AlertDialog(
                            onDismissRequest = { viewModel.clearOperationState() },
                            icon = {
                                Icon(
                                    Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            title = { Text(strings.errorTitle) },
                            text = { Text(op.message) },
                            confirmButton = {
                                Button(onClick = { viewModel.clearOperationState() }) {
                                    Text(strings.done)
                                }
                            }
                        )
                    }

                    AsyncOperationState.Idle -> {}
                }
            }
        }
    }
}
