# Hide source names and internal logging in release builds
-renamesourcefileattribute SourceFile
-repackageclasses 'o'
-allowaccessmodification

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# Keep core models & JSON serialization structures
-keep class com.example.engine.PresentationDocument { *; }
-keep class com.example.engine.SlideDocument { *; }
-keep class com.example.engine.SlideElement { *; }
-keep class com.example.engine.SlideBackground { *; }
-keep class com.example.engine.SlideTransition { *; }
-keep class com.example.engine.ElementAnimation { *; }
-keep class com.example.engine.TextStyleData { *; }
-keep class com.example.engine.ChartEntry { *; }
-keep class com.example.engine.BackgroundType { *; }
-keep class com.example.engine.SlideAspectRatio { *; }
-keep class com.example.engine.SlideTransitionType { *; }
-keep class com.example.engine.ElementAnimationType { *; }
-keep class com.example.engine.AnimationDirection { *; }
-keep class com.example.engine.ShapeType { *; }
-keep class com.example.engine.ChartType { *; }
-keep class com.example.engine.TextAlignMode { *; }
-keep class com.example.engine.VerticalAlignMode { *; }

# Keep Room entities & DAOs
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep interface * {
    @androidx.room.Query <methods>;
    @androidx.room.Insert <methods>;
    @androidx.room.Update <methods>;
    @androidx.room.Delete <methods>;
    @androidx.room.Upsert <methods>;
}

# Keep Firebase & Firestore annotated models
-keepattributes Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}
-keep @com.google.firebase.firestore.IgnoreExtraProperties class * { *; }

# WorkManager Workers & Biometric
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.biometric.** { *; }

# Credential Manager & Play Services Auth
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Coroutines & Serialization
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn org.slf4j.**
