package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.CoPresent
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.localization.LocalAppStrings
import com.example.engine.AnimationDirection
import com.example.engine.ElementAnimationType
import com.example.engine.Presentation
import com.example.engine.Slide
import com.example.engine.SlideBackground
import com.example.engine.SlideElement
import com.example.engine.SlideTransitionType
import com.example.engine.parseColorInt
import com.example.ui.editor.OrnamentDecorationLayer
import com.example.ui.editor.SlideElementContent
import com.example.ui.editor.SlideMiniPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.min

@Composable
fun PresentationPlayerScreen(
    presentation: Presentation,
    initialSlideIndex: Int = 0,
    onExit: () -> Unit
) {
    val strings = LocalAppStrings.current
    val slides = presentation.slides
    if (slides.isEmpty()) {
        onExit()
        return
    }

    BackHandler(onBack = onExit)

    var currentIndex by remember { mutableIntStateOf(initialSlideIndex.coerceIn(0, slides.lastIndex)) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var showHud by remember { mutableStateOf(true) }
    var showNotes by remember { mutableStateOf(false) }
    var presenterMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    val currentSlide = slides[currentIndex]
    val nextSlide = slides.getOrNull(currentIndex + 1)
    val timerText = remember(elapsedSeconds) {
        val m = elapsedSeconds / 60
        val s = elapsedSeconds % 60
        "%02d:%02d".format(m, s)
    }
    val ratio = (presentation.canvasWidth / presentation.canvasHeight).coerceIn(0.6f, 2.2f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .pointerInput(slides.size, currentIndex) {
                detectTapGestures(
                    onTap = { offset ->
                        when {
                            offset.x > size.width * 0.75f && currentIndex < slides.lastIndex -> currentIndex++
                            offset.x < size.width * 0.25f && currentIndex > 0 -> currentIndex--
                            else -> showHud = !showHud
                        }
                    }
                )
            }
            .testTag("presentation_player_root")
    ) {
        if (presenterMode) {
            // Presenter View: Live Slide Stage + Next Slide Preview + Quick Notes Access
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 74.dp, bottom = 92.dp, start = 12.dp, end = 12.dp)
            ) {
                val isWide = maxWidth >= 680.dp
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.7f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        ) {
                            AnimatedSlidePlayback(
                                presentation = presentation,
                                slides = slides,
                                currentIndex = currentIndex
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF111827),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            PresenterSidePanel(
                                presentation = presentation,
                                nextSlide = nextSlide,
                                ratio = ratio,
                                notes = currentSlide.speakerNotes,
                                showNotesOverlay = showNotes,
                                onToggleNotesOverlay = { showNotes = !showNotes }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        ) {
                            AnimatedSlidePlayback(
                                presentation = presentation,
                                slides = slides,
                                currentIndex = currentIndex
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF111827),
                            modifier = Modifier
                                .weight(0.9f)
                                .fillMaxWidth()
                        ) {
                            PresenterSidePanel(
                                presentation = presentation,
                                nextSlide = nextSlide,
                                ratio = ratio,
                                notes = currentSlide.speakerNotes,
                                showNotesOverlay = showNotes,
                                onToggleNotesOverlay = { showNotes = !showNotes }
                            )
                        }
                    }
                }
            }
        } else {
            AnimatedSlidePlayback(
                presentation = presentation,
                slides = slides,
                currentIndex = currentIndex
            )
        }

        // Top HUD Overlay
        AnimatedVisibility(
            visible = showHud || presenterMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.76f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    IconButton(onClick = onExit, modifier = Modifier.testTag("player_exit_btn")) {
                        Icon(Icons.Default.Close, contentDescription = strings.exitPresentation, tint = Color.White)
                    }
                    Text(
                        text = "${currentIndex + 1} / ${slides.size}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = strings.slideTimer, tint = Color(0xFF93C5FD), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(timerText, color = Color.White)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = presenterMode,
                            onClick = { presenterMode = !presenterMode },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.CoPresent,
                                    contentDescription = strings.presenterModeLabel,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(strings.presenterModeLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.12f),
                                labelColor = Color.White,
                                iconColor = Color.White,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("player_presenter_mode_btn")
                        )
                        IconButton(
                            onClick = { showNotes = !showNotes },
                            modifier = Modifier.testTag("player_notes_btn")
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = if (showNotes) strings.hideNotesOverlay else strings.showNotesOverlay,
                                tint = if (showNotes) Color(0xFFFBBF24) else Color.White
                            )
                        }
                    }
                }
            }
        }

        // Toggleable Custom Speaker Notes Overlay (populated from stored slide.speakerNotes)
        AnimatedVisibility(
            visible = showNotes,
            enter = fadeIn() + slideInVertically { it / 4 },
            exit = fadeOut() + slideOutVertically { it / 4 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xF20F172A),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x66FBBF24), RoundedCornerShape(18.dp))
                    .testTag("player_notes_overlay")
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.showNotesOverlay} • #${currentIndex + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                        TextButton(onClick = { showNotes = false }) {
                            Text(strings.hideNotesOverlay, color = Color(0xFF93C5FD))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = currentSlide.speakerNotes.ifBlank { strings.noSpeakerNotesForSlide },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("player_notes_text")
                    )
                }
            }
        }

        // Bottom Navigation HUD
        AnimatedVisibility(
            visible = showHud || presenterMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.76f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / slides.size.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { if (currentIndex > 0) currentIndex-- },
                            enabled = currentIndex > 0,
                            modifier = Modifier.testTag("player_prev_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(strings.prevSlide)
                        }
                        Text(
                            text = presentation.title,
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        FilledTonalButton(
                            onClick = { if (currentIndex < slides.lastIndex) currentIndex++ else onExit() },
                            modifier = Modifier.testTag("player_next_btn")
                        ) {
                            Text(if (currentIndex < slides.lastIndex) strings.nextSlide else strings.exitPresentation)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresenterSidePanel(
    presentation: Presentation,
    nextSlide: Slide?,
    ratio: Float,
    notes: String,
    showNotesOverlay: Boolean,
    onToggleNotesOverlay: () -> Unit
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = strings.nextSlidePreview,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF93C5FD),
            fontWeight = FontWeight.Bold
        )
        if (nextSlide != null) {
            SlideMiniPreview(
                presentation = presentation,
                slide = nextSlide,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(strings.exitPresentation, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
            }
        }

        FilledTonalButton(
            onClick = onToggleNotesOverlay,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (showNotesOverlay) strings.hideNotesOverlay else strings.showNotesOverlay)
        }

        if (!showNotesOverlay) {
            Text(
                text = notes.ifBlank { strings.noSpeakerNotesForSlide },
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AnimatedSlidePlayback(
    presentation: Presentation,
    slides: List<Slide>,
    currentIndex: Int
) {
    AnimatedContent(
        targetState = currentIndex,
        transitionSpec = {
            val targetSlide = slides[targetState]
            val dur = targetSlide.transition.durationMs.coerceIn(150, 1400)
            when (targetSlide.transition.type) {
                SlideTransitionType.NONE -> fadeIn(tween(1)) togetherWith fadeOut(tween(1))
                SlideTransitionType.FADE -> fadeIn(tween(dur)) togetherWith fadeOut(tween(dur))
                SlideTransitionType.PUSH -> {
                    if (targetState > initialState) {
                        (slideInHorizontally(tween(dur)) { it } + fadeIn(tween(dur))) togetherWith
                            (slideOutHorizontally(tween(dur)) { -it } + fadeOut(tween(dur)))
                    } else {
                        (slideInHorizontally(tween(dur)) { -it } + fadeIn(tween(dur))) togetherWith
                            (slideOutHorizontally(tween(dur)) { it } + fadeOut(tween(dur)))
                    }
                }
                SlideTransitionType.WIPE -> {
                    slideInHorizontally(tween(dur)) { it / 2 } + fadeIn(tween(dur)) togetherWith fadeOut(tween(dur))
                }
                SlideTransitionType.SPLIT -> {
                    (scaleIn(tween(dur), initialScale = 0.85f) + fadeIn(tween(dur))) togetherWith fadeOut(tween(dur))
                }
                SlideTransitionType.REVEAL -> {
                    (slideInVertically(tween(dur)) { it / 3 } + fadeIn(tween(dur))) togetherWith
                        (slideOutVertically(tween(dur)) { -it / 3 } + fadeOut(tween(dur)))
                }
                SlideTransitionType.ZOOM -> {
                    (scaleIn(tween(dur), initialScale = 0.65f) + fadeIn(tween(dur))) togetherWith fadeOut(tween(dur))
                }
                SlideTransitionType.SLIDE_UP -> {
                    (slideInVertically(tween(dur)) { it } + fadeIn(tween(dur))) togetherWith
                        (slideOutVertically(tween(dur)) { -it / 2 } + fadeOut(tween(dur)))
                }
                SlideTransitionType.SLIDE_DOWN -> {
                    (slideInVertically(tween(dur)) { -it } + fadeIn(tween(dur))) togetherWith
                        (slideOutVertically(tween(dur)) { it / 2 } + fadeOut(tween(dur)))
                }
                SlideTransitionType.FLIP -> {
                    (scaleIn(tween(dur), initialScale = 0.82f) + fadeIn(tween(dur))) togetherWith
                        fadeOut(tween(dur / 2))
                }
                SlideTransitionType.DISSOLVE -> {
                    fadeIn(tween((dur * 1.25f).toInt())) togetherWith fadeOut(tween((dur * 1.25f).toInt()))
                }
                SlideTransitionType.STEALTH -> {
                    fadeIn(tween((dur * 0.7f).toInt().coerceAtLeast(120))) togetherWith
                        fadeOut(tween((dur * 0.7f).toInt().coerceAtLeast(120)))
                }
                SlideTransitionType.EXCITING -> {
                    (scaleIn(tween(dur), initialScale = 0.62f) + slideInHorizontally(tween(dur)) { it / 2 } + fadeIn(tween(dur))) togetherWith
                        (fadeOut(tween(dur / 2)))
                }
                SlideTransitionType.DYNAMIC -> {
                    (slideInHorizontally(tween(dur)) { it } + slideInVertically(tween(dur)) { it / 6 } + fadeIn(tween(dur))) togetherWith
                        (slideOutHorizontally(tween(dur)) { -it / 2 } + fadeOut(tween(dur)))
                }
                SlideTransitionType.SCIENTIFIC -> {
                    (scaleIn(tween(dur), initialScale = 0.92f) + fadeIn(tween(dur))) togetherWith
                        fadeOut(tween(dur))
                }
                SlideTransitionType.ACADEMIC -> {
                    (slideInVertically(tween(dur)) { it / 6 } + fadeIn(tween(dur))) togetherWith
                        fadeOut(tween(dur))
                }
                SlideTransitionType.CURTAIN -> {
                    (slideInHorizontally(tween(dur)) { -it / 2 } + scaleIn(tween(dur), initialScale = 0.9f) + fadeIn(tween(dur))) togetherWith
                        (slideOutHorizontally(tween(dur)) { it / 2 } + fadeOut(tween(dur)))
                }
                SlideTransitionType.MORPH -> {
                    (scaleIn(tween(dur), initialScale = 0.95f) + fadeIn(tween(dur))) togetherWith
                        fadeOut(tween(dur))
                }
            }
        },
        label = "slide_playback_transition",
        modifier = Modifier.fillMaxSize()
    ) { idx ->
        AnimatedSlideStage(
            presentation = presentation,
            slide = slides[idx],
            slideKey = "${slides[idx].id}_$idx"
        )
    }
}

@Composable
private fun AnimatedSlideStage(
    presentation: Presentation,
    slide: Slide,
    slideKey: String
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val maxW = with(density) { maxWidth.toPx() }.coerceAtLeast(100f)
        val maxH = with(density) { maxHeight.toPx() }.coerceAtLeast(100f)
        val scale = min(maxW / presentation.canvasWidth, maxH / presentation.canvasHeight)
        val renderedW = with(density) { (presentation.canvasWidth * scale).toDp() }
        val renderedH = with(density) { (presentation.canvasHeight * scale).toDp() }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .width(renderedW)
                    .height(renderedH)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                when (val bg = slide.background) {
                    is SlideBackground.Solid -> Box(
                        Modifier.fillMaxSize().background(Color(parseColorInt(bg.colorHex, 0xFFFFFFFF.toInt())))
                    )
                    is SlideBackground.Gradient -> Box(
                        Modifier.fillMaxSize().background(
                            Brush.linearGradient(
                                listOf(Color(parseColorInt(bg.startColorHex)), Color(parseColorInt(bg.endColorHex)))
                            )
                        )
                    )
                    is SlideBackground.Image -> {
                        val file = File(bg.localImagePath)
                        if (file.exists()) {
                            AsyncImage(
                                model = file,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Box(Modifier.fillMaxSize().background(Color(parseColorInt(bg.overlayColorHex, 0x33000000))))
                    }
                }
                OrnamentDecorationLayer(
                    decorationId = slide.decorationId,
                    decorationColorHex = slide.decorationColorHex
                )

                val sorted = remember(slide.elements) { slide.elements.sortedBy { it.zIndex } }
                sorted.forEachIndexed { idx, el ->
                    AnimatedStageElement(
                        element = el,
                        scale = scale,
                        slideKey = slideKey,
                        staggerMs = idx * 80
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedStageElement(
    element: SlideElement,
    scale: Float,
    slideKey: String,
    staggerMs: Int
) {
    val density = LocalDensity.current
    val animSpec = element.animation
    val alphaAnim = remember(slideKey, element.id) {
        Animatable(if (animSpec.type == ElementAnimationType.NONE) 1f else 0f)
    }
    val offsetXAnim = remember(slideKey, element.id) { Animatable(0f) }
    val offsetYAnim = remember(slideKey, element.id) { Animatable(0f) }
    val scaleAnim = remember(slideKey, element.id) {
        Animatable(if (animSpec.type == ElementAnimationType.ZOOM_IN || animSpec.type == ElementAnimationType.BOUNCE) 0.5f else 1f)
    }
    val rotOffsetAnim = remember(slideKey, element.id) {
        Animatable(if (animSpec.type == ElementAnimationType.ROTATE) -35f else 0f)
    }

    LaunchedEffect(slideKey, element.id) {
        if (animSpec.type == ElementAnimationType.NONE) return@LaunchedEffect
        val totalDelay = (animSpec.delayMs + staggerMs).toLong().coerceAtLeast(0L)
        val dur = animSpec.durationMs.coerceIn(150, 2000)
        val rangeScale = animSpec.motionRange.scaleFactor

        when (animSpec.type) {
            ElementAnimationType.FLY_IN, ElementAnimationType.EXIT_FLY -> {
                when (animSpec.direction) {
                    AnimationDirection.FROM_LEFT -> offsetXAnim.snapTo(-180f * rangeScale)
                    AnimationDirection.FROM_RIGHT -> offsetXAnim.snapTo(180f * rangeScale)
                    AnimationDirection.FROM_TOP -> offsetYAnim.snapTo(-140f * rangeScale)
                    AnimationDirection.FROM_BOTTOM -> offsetYAnim.snapTo(140f * rangeScale)
                }
            }
            ElementAnimationType.FLOAT -> offsetYAnim.snapTo(60f * rangeScale)
            ElementAnimationType.ORBIT_PATH -> {
                offsetXAnim.snapTo(-90f * rangeScale)
                offsetYAnim.snapTo(60f * rangeScale)
            }
            ElementAnimationType.PULSE_EMPHASIS -> scaleAnim.snapTo(0.86f)
            ElementAnimationType.SWING_EMPHASIS -> rotOffsetAnim.snapTo(-12f * rangeScale)
            else -> {}
        }

        if (totalDelay > 0L) delay(totalDelay)

        launch { alphaAnim.animateTo(1f, tween(dur)) }
        launch { offsetXAnim.animateTo(0f, tween(dur)) }
        launch { offsetYAnim.animateTo(0f, tween(dur)) }
        launch {
            if (animSpec.type == ElementAnimationType.BOUNCE || animSpec.type == ElementAnimationType.PULSE_EMPHASIS) {
                scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            } else {
                scaleAnim.animateTo(1f, tween(dur))
            }
        }
        launch { rotOffsetAnim.animateTo(0f, tween(dur)) }
    }

    val elX = with(density) { (element.x * scale).toDp() }
    val elY = with(density) { (element.y * scale).toDp() }
    val elW = with(density) { (element.width * scale).toDp() }
    val elH = with(density) { (element.height * scale).toDp() }

    Box(
        modifier = Modifier
            .offset(x = elX + offsetXAnim.value.dp, y = elY + offsetYAnim.value.dp)
            .size(width = elW, height = elH)
            .scale(scaleAnim.value)
            .rotate(element.rotation + rotOffsetAnim.value)
            .alpha((element.opacity * alphaAnim.value).coerceIn(0f, 1f))
    ) {
        SlideElementContent(element = element, scale = scale)
    }
}
