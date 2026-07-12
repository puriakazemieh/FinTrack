# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Kotlinx Serialization
-keep @kotlinx.serialization.Serializable class ** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}

# Koin
-keep class org.koin.** { *; }

# SQLDelight
-keep class com.kazemieh.database.** { *; }
-keep class app.cash.sqldelight.** { *; }

# Glance
-keep class androidx.glance.** { *; }
-keep class com.kazemieh.widget.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# General KMP
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# WorkManager
-keep class androidx.work.impl.WorkDatabase_Impl {
    public <init>(...);
}
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.work.impl.** { *; }

# Room (used by WorkManager)
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}

# DataStore & Shaded Protobuf (WorkManager 2.10+ internal dependency)
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.protobuf.** { *; }

# App Startup
-keep class androidx.startup.** { *; }
