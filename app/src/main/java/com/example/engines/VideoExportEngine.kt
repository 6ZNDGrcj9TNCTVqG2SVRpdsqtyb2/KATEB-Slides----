package com.example.engines

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import com.example.engine.Presentation
import com.example.engine.Slide
import com.example.engine.SlideTransitionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.min

enum class VideoResolution(val label: String, val width: Int, val height: Int, val bitrate: Int) {
    HD_720P("720p HD", 1280, 720, 4_000_000),
    FHD_1080P("1080p Full HD", 1920, 1080, 8_000_000)
}

object VideoExportEngine {

    suspend fun exportToMp4(
        context: Context,
        presentation: Presentation,
        outputFile: File,
        resolution: VideoResolution = VideoResolution.HD_720P,
        secondsPerSlide: Int = 4,
        audioFilePath: String? = null,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        outputFile.parentFile?.mkdirs()
        if (presentation.slides.isEmpty()) {
            throw IllegalStateException("empty_presentation_slides")
        }

        val width = resolution.width
        val height = resolution.height
        val fps = 24
        val slideDurationSec = secondsPerSlide.coerceIn(1, 30)
        val framesPerSlide = fps * slideDurationSec
        val transitionFrames = (fps * 0.5f).toInt().coerceAtLeast(1)

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, resolution.bitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = codec.createInputSurface()
        codec.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var videoTrackIndex = -1
        var audioTrackIndex = -1
        var muxerStarted = false

        val audioExtractor = if (!audioFilePath.isNullOrBlank() && File(audioFilePath).exists()) {
            runCatching {
                MediaExtractor().apply { setDataSource(audioFilePath) }
            }.getOrNull()
        } else null

        var selectedAudioSourceTrack = -1
        var audioFormat: MediaFormat? = null
        if (audioExtractor != null) {
            for (i in 0 until audioExtractor.trackCount) {
                val tf = audioExtractor.getTrackFormat(i)
                val mime = tf.getString(MediaFormat.KEY_MIME).orEmpty()
                if (mime.startsWith("audio/")) {
                    selectedAudioSourceTrack = i
                    audioFormat = tf
                    audioExtractor.selectTrack(i)
                    break
                }
            }
        }

        val bufferInfo = MediaCodec.BufferInfo()
        val slideCount = presentation.slides.size
        val totalFrames = (slideCount * framesPerSlide).coerceAtLeast(1)
        var frameCounter = 0

        // Rolling 2-bitmap buffer avoids OOM even for 50-slide presentations at 1080p.
        var currentBmp: Bitmap? = null
        var nextBmp: Bitmap? = null

        try {
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            val dstRect = Rect(0, 0, width, height)

            currentBmp = renderSlideFittedBitmap(context, presentation, presentation.slides[0], width, height)
            for (sIdx in presentation.slides.indices) {
                val curr = currentBmp ?: renderSlideFittedBitmap(context, presentation, presentation.slides[sIdx], width, height)
                nextBmp = if (sIdx + 1 < slideCount) {
                    renderSlideFittedBitmap(context, presentation, presentation.slides[sIdx + 1], width, height)
                } else null

                val nextTransitionType = presentation.slides.getOrNull(sIdx + 1)?.transition?.type
                    ?: presentation.slides[sIdx].transition.type

                for (f in 0 until framesPerSlide) {
                    val canvas = inputSurface.lockCanvas(null)
                    try {
                        paint.alpha = 255
                        if (nextBmp != null && f >= framesPerSlide - transitionFrames) {
                            val rawT = (f - (framesPerSlide - transitionFrames)).toFloat() / transitionFrames.toFloat()
                            val t = easeInOut(rawT.coerceIn(0f, 1f))
                            drawTransitionFrame(
                                canvas = canvas,
                                currentBmp = curr,
                                nextBmp = nextBmp,
                                width = width,
                                height = height,
                                progress = t,
                                transitionType = nextTransitionType,
                                paint = paint
                            )
                        } else {
                            canvas.drawBitmap(curr, null, dstRect, paint)
                        }
                    } finally {
                        inputSurface.unlockCanvasAndPost(canvas)
                    }

                    frameCounter++
                    onProgress((frameCounter.toFloat() / totalFrames.toFloat()).coerceIn(0f, 0.92f))

                    drainEncoder(
                        codec = codec,
                        muxer = muxer,
                        bufferInfo = bufferInfo,
                        endOfStream = false,
                        onFormatChanged = { outFormat ->
                            videoTrackIndex = muxer.addTrack(outFormat)
                            if (audioFormat != null) {
                                audioTrackIndex = runCatching { muxer.addTrack(audioFormat) }.getOrDefault(-1)
                            }
                            muxer.start()
                            muxerStarted = true
                        },
                        isMuxerStarted = { muxerStarted },
                        getVideoTrack = { videoTrackIndex }
                    )
                }

                curr.recycle()
                currentBmp = nextBmp
                nextBmp = null
            }

            codec.signalEndOfInputStream()
            drainEncoder(
                codec = codec,
                muxer = muxer,
                bufferInfo = bufferInfo,
                endOfStream = true,
                onFormatChanged = { outFormat ->
                    if (!muxerStarted) {
                        videoTrackIndex = muxer.addTrack(outFormat)
                        if (audioFormat != null) {
                            audioTrackIndex = runCatching { muxer.addTrack(audioFormat) }.getOrDefault(-1)
                        }
                        muxer.start()
                        muxerStarted = true
                    }
                },
                isMuxerStarted = { muxerStarted },
                getVideoTrack = { videoTrackIndex }
            )

            // Mux audio up to video total duration
            if (muxerStarted && audioExtractor != null && selectedAudioSourceTrack >= 0 && audioTrackIndex >= 0) {
                val totalDurationUs = (totalFrames.toLong() * 1_000_000L) / fps
                val audioBuf = ByteBuffer.allocate(256 * 1024)
                val audioInfo = MediaCodec.BufferInfo()
                while (true) {
                    val sampleSize = audioExtractor.readSampleData(audioBuf, 0)
                    if (sampleSize < 0) break
                    val pts = audioExtractor.sampleTime
                    if (pts > totalDurationUs) break
                    audioInfo.offset = 0
                    audioInfo.size = sampleSize
                    audioInfo.presentationTimeUs = pts
                    audioInfo.flags = audioExtractor.sampleFlags
                    muxer.writeSampleData(audioTrackIndex, audioBuf, audioInfo)
                    audioExtractor.advance()
                }
            }
            onProgress(1f)
        } finally {
            runCatching { currentBmp?.recycle() }
            runCatching { nextBmp?.recycle() }
            runCatching { audioExtractor?.release() }
            runCatching { codec.stop() }
            runCatching { codec.release() }
            runCatching { inputSurface.release() }
            if (muxerStarted) {
                runCatching { muxer.stop() }
            }
            runCatching { muxer.release() }
        }
        outputFile
    }

    private fun renderSlideFittedBitmap(
        context: Context,
        presentation: Presentation,
        slide: Slide,
        frameWidth: Int,
        frameHeight: Int
    ): Bitmap {
        val frameBmp = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(frameBmp)
        canvas.drawColor(0xFF0B1120.toInt())

        val cw = presentation.canvasWidth.coerceAtLeast(1f)
        val ch = presentation.canvasHeight.coerceAtLeast(1f)
        val fitScale = min(frameWidth / cw, frameHeight / ch)
        val drawW = cw * fitScale
        val drawH = ch * fitScale
        val offsetX = (frameWidth - drawW) / 2f
        val offsetY = (frameHeight - drawH) / 2f

        canvas.save()
        canvas.translate(offsetX, offsetY)
        SlideBitmapRenderer.drawSlideOnCanvas(
            context = context,
            presentation = presentation,
            slide = slide,
            canvas = canvas,
            targetWidth = drawW,
            targetHeight = drawH
        )
        canvas.restore()
        return frameBmp
    }

    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        onFormatChanged: (MediaFormat) -> Unit,
        isMuxerStarted: () -> Boolean,
        getVideoTrack: () -> Int
    ) {
        val timeoutUs = if (endOfStream) 10_000L else 0L
        var eosWaitPolls = 0
        val maxEosWaitPolls = 450
        while (true) {
            val outIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            when {
                outIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!endOfStream) break
                    eosWaitPolls++
                    if (eosWaitPolls >= maxEosWaitPolls) break
                }
                outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (!isMuxerStarted()) {
                        onFormatChanged(codec.outputFormat)
                    }
                }
                outIndex >= 0 -> {
                    eosWaitPolls = 0
                    val encodedData = codec.getOutputBuffer(outIndex) ?: continue
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size != 0 && isMuxerStarted()) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(getVideoTrack(), encodedData, bufferInfo)
                    }
                    codec.releaseOutputBuffer(outIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
                }
            }
        }
    }

    private fun easeInOut(t: Float): Float = t * t * (3f - 2f * t)

    private fun drawTransitionFrame(
        canvas: Canvas,
        currentBmp: Bitmap,
        nextBmp: Bitmap,
        width: Int,
        height: Int,
        progress: Float,
        transitionType: SlideTransitionType,
        paint: Paint
    ) {
        val fullDst = Rect(0, 0, width, height)
        when (transitionType) {
            SlideTransitionType.NONE -> {
                paint.alpha = 255
                canvas.drawBitmap(currentBmp, null, fullDst, paint)
            }
            SlideTransitionType.FADE, SlideTransitionType.DISSOLVE -> {
                paint.alpha = 255
                canvas.drawBitmap(currentBmp, null, fullDst, paint)
                paint.alpha = (progress * 255f).toInt().coerceIn(0, 255)
                canvas.drawBitmap(nextBmp, null, fullDst, paint)
                paint.alpha = 255
            }
            SlideTransitionType.PUSH -> {
                val shift = (progress * width).toInt()
                paint.alpha = 255
                canvas.drawBitmap(currentBmp, null, Rect(-shift, 0, width - shift, height), paint)
                canvas.drawBitmap(nextBmp, null, Rect(width - shift, 0, width * 2 - shift, height), paint)
            }
            SlideTransitionType.SLIDE_UP -> {
                val shiftY = (progress * height).toInt()
                paint.alpha = 255
                canvas.drawBitmap(currentBmp, null, Rect(0, -shiftY, width, height - shiftY), paint)
                canvas.drawBitmap(nextBmp, null, Rect(0, height - shiftY, width, height * 2 - shiftY), paint)
            }
            SlideTransitionType.SLIDE_DOWN -> {
                val shiftY = (progress * height).toInt()
                paint.alpha = 255
                canvas.drawBitmap(currentBmp, null, Rect(0, shiftY, width, height + shiftY), paint)
                canvas.drawBitmap(nextBmp, null, Rect(0, shiftY - height, width, shiftY), paint)
            }
            SlideTransitionType.FLIP -> {
                canvas.drawColor(0xFF0B1120.toInt())
                val factor = abs(progress - 0.5f) * 2f
                val activeBmp = if (progress < 0.5f) currentBmp else nextBmp
                val halfW = (width * factor.coerceAtLeast(0.04f)) / 2f
                val cx = width / 2f
                paint.alpha = 255
                canvas.drawBitmap(activeBmp, null, RectF(cx - halfW, 0f, cx + halfW, height.toFloat()), paint)
            }
            SlideTransitionType.WIPE -> {
                paint.alpha = 255
                canvas.drawBitmap(currentBmp, null, fullDst, paint)
                val wipeRight = (progress * width).toInt().coerceIn(0, width)
                canvas.save()
                canvas.clipRect(0, 0, wipeRight, height)
                canvas.drawBitmap(nextBmp, null, fullDst, paint)
                canvas.restore()
            }
            SlideTransitionType.SPLIT -> {
                paint.alpha = 255
                canvas.drawBitmap(currentBmp, null, fullDst, paint)
                val halfSpan = ((progress * width) / 2f).toInt().coerceIn(0, width / 2)
                val mid = width / 2
                canvas.save()
                canvas.clipRect(mid - halfSpan, 0, mid + halfSpan, height)
                canvas.drawBitmap(nextBmp, null, fullDst, paint)
                canvas.restore()
            }
            SlideTransitionType.REVEAL -> {
                paint.alpha = 255
                canvas.drawBitmap(nextBmp, null, fullDst, paint)
                val slideOutX = (progress * width).toInt()
                paint.alpha = ((1f - progress * 0.3f) * 255f).toInt().coerceIn(0, 255)
                canvas.drawBitmap(currentBmp, null, Rect(-slideOutX, 0, width - slideOutX, height), paint)
                paint.alpha = 255
            }
            SlideTransitionType.ZOOM, SlideTransitionType.EXCITING -> {
                paint.alpha = 255
                canvas.drawBitmap(currentBmp, null, fullDst, paint)
                val scale = if (transitionType == SlideTransitionType.EXCITING) 0.55f + 0.45f * progress else 0.65f + 0.35f * progress
                val dstW = width * scale
                val dstH = height * scale
                val left = (width - dstW) / 2f
                val top = (height - dstH) / 2f
                paint.alpha = (progress * 255f).toInt().coerceIn(0, 255)
                canvas.drawBitmap(nextBmp, null, RectF(left, top, left + dstW, top + dstH), paint)
                paint.alpha = 255
            }
            SlideTransitionType.STEALTH, SlideTransitionType.ACADEMIC -> {
                paint.alpha = 255
                canvas.drawBitmap(currentBmp, null, fullDst, paint)
                paint.alpha = ((progress * progress) * 255f).toInt().coerceIn(0, 255)
                canvas.drawBitmap(nextBmp, null, fullDst, paint)
                paint.alpha = 255
            }
            SlideTransitionType.DYNAMIC, SlideTransitionType.MORPH -> {
                val shift = ((1f - progress) * width * 0.14f).toInt()
                paint.alpha = ((1f - progress) * 255f).toInt().coerceIn(0, 255)
                canvas.drawBitmap(currentBmp, null, Rect(-shift, 0, width - shift, height), paint)
                paint.alpha = (progress * 255f).toInt().coerceIn(0, 255)
                canvas.drawBitmap(nextBmp, null, Rect(shift, 0, width + shift, height), paint)
                paint.alpha = 255
            }
            SlideTransitionType.SCIENTIFIC -> {
                paint.alpha = 255
                canvas.drawBitmap(currentBmp, null, fullDst, paint)
                val revealBottom = (progress * height).toInt().coerceIn(0, height)
                canvas.save()
                canvas.clipRect(0, 0, width, revealBottom)
                canvas.drawBitmap(nextBmp, null, fullDst, paint)
                canvas.restore()
            }
            SlideTransitionType.CURTAIN -> {
                paint.alpha = 255
                canvas.drawBitmap(nextBmp, null, fullDst, paint)
                val openW = ((width / 2f) * (1f - progress)).toInt().coerceAtLeast(0)
                if (openW > 0) {
                    canvas.save()
                    canvas.clipRect(0, 0, openW, height)
                    canvas.drawBitmap(currentBmp, null, fullDst, paint)
                    canvas.restore()
                    canvas.save()
                    canvas.clipRect(width - openW, 0, width, height)
                    canvas.drawBitmap(currentBmp, null, fullDst, paint)
                    canvas.restore()
                }
            }
        }
    }
}
