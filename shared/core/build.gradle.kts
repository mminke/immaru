import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        browser {
//            commonWebpackConfig {
//                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
//                    static = (static ?: mutableListOf()).apply {
//                        // Serve sources to debug inside browser
//                        add(project.projectDir.path)
//                    }
//                }
//            }
//        }
//    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }

        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.io.core)
            api(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.reactive)
            implementation(libs.microutils.kotlin.logging.jvm)
            implementation(libs.apache.tika.core)
            compileOnly(libs.r2dbc.spi)
        }
        jvmTest.dependencies {
            implementation(libs.junit.jupiter)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.flyway.core)
            implementation("org.testcontainers:postgresql:1.20.4")
            implementation("org.testcontainers:r2dbc:1.20.4")

            implementation("org.slf4j:slf4j-simple:2.0.16")

            runtimeOnly(libs.postgresql.jdbc)
            runtimeOnly(libs.postgresql.r2dbc)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

android {
    namespace = "com.earthrevealed.immaru.core"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

// This buildscript is a temporary workaround to make Android Studio run the immaru-server jvm
// with the proper flyway scripts. Android studio does not include the resources folder of dependencies
// so the resources are copied directly to the classes folder by this script.
// see: https://youtrack.jetbrains.com/issue/KTIJ-16582/Consumer-Kotlin-JVM-library-cannot-access-a-Kotlin-Multiplatform-JVM-target-resources-in-multi-module-Gradle-project\
tasks {
    val jvmProcessResources by getting
    val fixMissingResources by creating(Copy::class) {
        dependsOn(jvmProcessResources)
        from("$buildDir/processedResources/jvm/main")
        into("$buildDir/classes/kotlin/jvm/main")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    val jvmJar by getting(Jar::class) {
        dependsOn(fixMissingResources)
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}