rootProject.name = "Immaru"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }

//    versionCatalogs {
//        create("immaru") {
//            library("libs-common-uuid", "com.benasher44:uuid:0.8.4")
//        }
//    }
}

include(":composeApp")
include(":server")
include(":core")


project(":composeApp").projectDir = file("frontend/composeApp")
project(":server").projectDir = file("backend/server")
project(":core").projectDir = file("shared/core")
