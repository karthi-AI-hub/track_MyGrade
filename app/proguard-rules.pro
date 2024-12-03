# Firebase Crashlytics
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
-keep class com.google.firebase.analytics.** { *; }
-dontwarn com.google.firebase.analytics.**
-keep class com.google.firebase.analytics.FirebaseAnalytics { *; }
-keep class com.google.firebase.messaging.FirebaseMessaging { *; }
-keep class com.google.firebase.auth.FirebaseAuth { *; }
-keep class com.google.firebase.database.FirebaseDatabase { *; }
-keep class com.google.firebase.firestore.FirebaseFirestore { *; }

# Firebase Storage
-keep class com.google.firebase.storage.FirebaseStorage { *; }

# Firebase Remote Config
-keep class com.google.firebase.remoteconfig.FirebaseRemoteConfig { *; }

# Keep Firestore annotations
-keep @com.google.firebase.firestore.IgnoreExtraProperties class * { *; }
-keep @com.google.firebase.firestore.ServerTimestamp class * { *; }

# Keep classes used by Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep Firestore UserDataReader and other internal classes
-keep class com.google.firebase.firestore.UserDataReader { *; }
-keep class com.google.firebase.firestore.core.UserData { *; }
-keep class com.google.firebase.firestore.core.UserData$ParseContext { *; }

# Keep your custom Subject class if used with Firestore
-keep class com.student_developer.track_my_grade.Subject { *; }

# Keep classes with Firebase annotations (like @PropertyName, @ServerTimestamp, etc.)
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.ServerTimestamp <methods>;
    @com.google.firebase.firestore.IgnoreExtraProperties <methods>;
}

-keep class com.unity3d.ads.** { *; }
-keep class com.google.ads.mediation.unity.** { *; }

-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener
-dontwarn android.media.LoudnessCodecController
