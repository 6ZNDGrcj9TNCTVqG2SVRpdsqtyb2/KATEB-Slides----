package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.local.PresentationDao
import com.example.data.local.PresentationEntity
import com.example.data.local.ProjectStorageManager
import com.example.engine.Presentation
import com.example.engine.PresentationJsonSerializer
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

data class CloudAccountUser(
    val uid: String,
    val displayName: String,
    val email: String
)

class CloudSyncRepository(
    private val context: Context,
    private val dao: PresentationDao,
    private val storage: ProjectStorageManager
) {
    private fun hasFirebaseApp(): Boolean =
        FirebaseApp.getApps(context).isNotEmpty() ||
            runCatching { FirebaseApp.initializeApp(context) != null }.getOrDefault(false)

    private val authOrNull: FirebaseAuth?
        get() = if (hasFirebaseApp()) runCatching { FirebaseAuth.getInstance() }.getOrNull() else null

    private val firestore: FirebaseFirestore by lazy {
        if (!hasFirebaseApp()) error("firebase_unavailable")
        val dbId = context.getString(R.string.firestore_database_id)
        if (dbId.isNotBlank()) FirebaseFirestore.getInstance(dbId)
        else FirebaseFirestore.getInstance()
    }

    private val _currentUser = MutableStateFlow(authOrNull?.currentUser?.let {
        CloudAccountUser(
            uid = it.uid,
            displayName = it.displayName ?: it.email ?: "Account",
            email = it.email.orEmpty()
        )
    })
    val currentUser: Flow<CloudAccountUser?> = _currentUser.asStateFlow()

    fun refreshAuthState() {
        val user = authOrNull?.currentUser
        _currentUser.value = user?.let {
            CloudAccountUser(
                uid = it.uid,
                displayName = it.displayName ?: it.email ?: "Account",
                email = it.email.orEmpty()
            )
        }
    }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<CloudAccountUser> = withContext(Dispatchers.IO) {
        runCatching {
            val auth = authOrNull ?: error("firebase_unavailable")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: error("auth_missing")
            val account = CloudAccountUser(
                uid = user.uid,
                displayName = user.displayName ?: user.email ?: "Account",
                email = user.email.orEmpty()
            )
            _currentUser.value = account
            account
        }
    }

    fun signOut() {
        authOrNull?.signOut()
        _currentUser.value = null
    }

    suspend fun syncPendingPresentations(): Result<Int> = withContext(Dispatchers.IO) {
        val user = authOrNull?.currentUser ?: return@withContext Result.success(0)
        runCatching {
            val pending = dao.getPendingCloudSync()
            var syncedCount = 0
            for (entity in pending) {
                val uploaded = uploadPresentation(user.uid, entity)
                if (uploaded) {
                    dao.markCloudSynced(entity.id, System.currentTimeMillis(), user.uid)
                    syncedCount++
                }
            }
            pullRemotePresentations(user.uid)
            syncedCount
        }
    }

    private suspend fun uploadPresentation(uid: String, entity: PresentationEntity): Boolean {
        val jsonFile = File(storage.presentationJsonFile(entity.id).absolutePath)
        val rawJson = if (jsonFile.exists()) jsonFile.readText(Charsets.UTF_8) else ""
        if (rawJson.toByteArray(Charsets.UTF_8).size > 900_000) {
            return false
        }
        val docRef = firestore.collection("users")
            .document(uid)
            .collection("presentations")
            .document(entity.id)

        val payload = hashMapOf<String, Any?>(
            "userId" to uid,
            "title" to entity.title.take(160),
            "description" to entity.description.take(1000),
            "languageCode" to entity.languageCode.uppercase(),
            "aspectRatioId" to entity.aspectRatioId,
            "themeId" to entity.themeId,
            "sourceType" to entity.sourceType,
            "slideCount" to entity.slideCount,
            "folderTag" to entity.folderTag,
            "locked" to entity.isLocked,
            "deleted" to false,
            "payloadJson" to rawJson,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        docRef.set(payload, SetOptions.merge()).await()
        return true
    }

    private suspend fun pullRemotePresentations(uid: String) {
        val snapshot = firestore.collection("users")
            .document(uid)
            .collection("presentations")
            .whereEqualTo("deleted", false)
            .get()
            .await()

        for (doc in snapshot.documents) {
            val id = doc.id
            val payloadJson = doc.getString("payloadJson").orEmpty()
            if (payloadJson.isBlank()) continue
            val existing = dao.getById(id)
            val parsed: Presentation? = runCatching {
                PresentationJsonSerializer.fromJsonString(payloadJson)
            }.getOrNull()
            if (parsed == null) continue

            if (existing == null) {
                storage.ensureAllDirectories()
                storage.presentationJsonFile(id).writeText(payloadJson, Charsets.UTF_8)
                val entity = PresentationEntity(
                    id = id,
                    title = doc.getString("title") ?: parsed.title,
                    description = doc.getString("description") ?: parsed.description,
                    languageCode = (doc.getString("languageCode") ?: parsed.language.code).lowercase(),
                    aspectRatioId = doc.getString("aspectRatioId") ?: parsed.aspectRatio.id,
                    themeId = doc.getString("themeId") ?: parsed.themeId,
                    sourceType = doc.getString("sourceType") ?: parsed.sourceType.name,
                    slideCount = (doc.getLong("slideCount")?.toInt() ?: parsed.slides.size).coerceAtLeast(1),
                    thumbnailPath = storage.coverThumbnailFile(id).absolutePath,
                    projectDirPath = storage.projectDir(id).absolutePath,
                    createdAt = parsed.createdAt,
                    updatedAt = parsed.updatedAt,
                    lastOpenedAt = System.currentTimeMillis(),
                    hasPendingAutosave = false,
                    folderTag = doc.getString("folderTag") ?: "general",
                    isLocked = doc.getBoolean("locked") ?: false,
                    pendingCloudSync = false,
                    cloudSyncedAt = System.currentTimeMillis(),
                    updatedByUid = uid
                )
                dao.upsert(entity)
            }
        }
    }
}
