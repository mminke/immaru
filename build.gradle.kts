plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
//    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.serialization) apply false
}


// This buildscript is a temporary workaround to make the jib plugin work correctly.
// see: https://github.com/GoogleContainerTools/jib/issues/4235
buildscript {
    dependencies {
        classpath("commons-codec:commons-codec:1.21.0")
    }
    configurations.all {
        resolutionStrategy {
            force("org.apache.commons:commons-compress:1.28.0")
            force("commons-codec:commons-codec:1.21.0")
        }
    }
}