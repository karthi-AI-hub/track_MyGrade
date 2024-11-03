pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://maven.google.com")
        maven { url = uri("https://jitpack.io") }
        jcenter()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Prefer settings repositories
    repositories {
        google()
        mavenCentral()
        maven(url = "https://maven.google.com")
        maven { url = uri("https://jitpack.io") }
        jcenter()
    }
}

rootProject.name = "Track_My_Grade"
include(":app")
include(":uCrop")
project(":uCrop").projectDir = file("uCrop")

 