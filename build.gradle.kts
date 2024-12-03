// build.gradle.kts (Project-level)

// Top-level build file where you can add configuration options common to all sub-projects/modules.

// Plugin definitions for project modules
plugins {
    alias(libs.plugins.android.application) apply false // Android application plugin for all app modules
    id("com.google.gms.google-services") version "4.4.2" apply false // Google Services plugin for Firebase
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false

}
repositories {
    google()
    mavenCentral()
}

// Task to clean the project build directory
tasks.register<Delete>("clean") {
    delete(project.buildDir) // Deletes the build directory for a clean state
}
