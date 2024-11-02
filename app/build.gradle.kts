plugins {
    id("com.android.application")
}

android {
    namespace = "com.student_developer.track_my_grade"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.student_developer.track_my_grade"
        minSdk = 25
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true

    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")

    implementation("com.airbnb.android:lottie-compose:5.0.3")

    implementation("androidx.multidex:multidex:2.0.1")
    implementation(libs.material)
    implementation(libs.firebase.analytics)

    implementation("com.google.android.material:material:1.9.0")
    implementation(libs.firebase.database)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.fragment:fragment:1.5.5")


    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
}

apply(plugin = "com.google.gms.google-services")
