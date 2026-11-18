package com.example.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.core.localization.AppLanguage
import com.example.core.localization.stringsFor

/**
 * WorkManager Worker posting reliable localized system notifications when PPTX, PDF,
 * image, or MP4 exports finish or fail.
 */
class ExportNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val success = inputData.getBoolean(KEY_SUCCESS, true)
        val formatLabel = inputData.getString(KEY_FORMAT).orEmpty()
        val deckTitle = inputData.getString(KEY_DECK_TITLE).orEmpty()
        val lang = AppLanguage.fromCode(inputData.getString(KEY_LANG))
        val strings = stringsFor(lang)

        val title = if (success) strings.exportFinishedNotifTitle else strings.exportFailedNotifTitle
        val body = if (success) {
            listOf(deckTitle, formatLabel, strings.savedToPhoneStorage)
                .filter { it.isNotBlank() }
                .joinToString(" • ")
        } else {
            listOf(deckTitle, formatLabel, strings.errorTitle)
                .filter { it.isNotBlank() }
                .joinToString(" • ")
        }

        ExportNotificationHelper.showExportNotification(
            context = applicationContext,
            title = title,
            body = body,
            channelName = strings.exportNotifChannelName,
            isSuccess = success
        )
        return Result.success()
    }

    companion object {
        const val KEY_SUCCESS = "export_success"
        const val KEY_FORMAT = "export_format"
        const val KEY_DECK_TITLE = "export_deck_title"
        const val KEY_LANG = "export_lang"

        fun enqueue(
            context: Context,
            success: Boolean,
            formatLabel: String,
            deckTitle: String,
            language: AppLanguage
        ) {
            val data = Data.Builder()
                .putBoolean(KEY_SUCCESS, success)
                .putString(KEY_FORMAT, formatLabel)
                .putString(KEY_DECK_TITLE, deckTitle)
                .putString(KEY_LANG, language.code)
                .build()
            val request = OneTimeWorkRequestBuilder<ExportNotificationWorker>()
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
