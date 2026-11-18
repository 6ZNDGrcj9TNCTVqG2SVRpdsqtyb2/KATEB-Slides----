package com.example.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.KatebDatabase
import com.example.data.local.ProjectStorageManager
import com.example.data.remote.CloudSyncRepository

/**
 * Background WorkManager worker synchronizing locally saved presentations with Firebase Firestore
 * whenever connectivity is available and the user has signed in with Google.
 */
class CloudSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = KatebDatabase.getInstance(applicationContext)
        val repo = CloudSyncRepository(
            context = applicationContext,
            dao = db.presentationDao(),
            storage = ProjectStorageManager(applicationContext)
        )
        return repo.syncPendingPresentations().fold(
            onSuccess = { Result.success() },
            onFailure = { if (runAttemptCount < 3) Result.retry() else Result.failure() }
        )
    }

    companion object {
        private const val WORK_NAME = "kateb_cloud_sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<CloudSyncWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
