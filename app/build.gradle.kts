plugins {
    id("com.android.application")
    id("com.google.firebase.firebase-perf")
}

android {
    namespace = "com.student_developer.track_my_grade"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.student_developer.track_my_grade"
        minSdk = 25
        targetSdk = 34
        versionCode = 6
        versionName = "1.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

//    signingConfigs {
//        create("release") {
//            storeFile = file("E:/Track My Grade/MainKey.jks")
//            storePassword = "Admin@K1"
//            keyAlias = "1"
//            keyPassword = "Admin@K1"
//        }
//    }

    buildTypes {
        release {
//            isMinifyEnabled = true
//            isShrinkResources = true
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
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
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.perf)

    implementation(libs.play.services.ads.lite)
    implementation(libs.lottie.compose)

    implementation(libs.multidex)
    implementation(libs.material)
    implementation(libs.firebase.analytics)

    implementation(libs.material)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation (libs.appcompat)
    implementation (libs.fragment)
    implementation(project(":uCrop"))

    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation (libs.mpandroidchart)
}

apply(plugin = "com.google.gms.google-services")
