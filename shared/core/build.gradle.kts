import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.serialization)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        optIn.add("kotlin.time.ExperimentalTime")
    }

    androidLibrary {
        namespace = "com.earthrevealed.immaru.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

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
            implementation(libs.bundles.flyway)
            implementation(libs.testcontainers.postgresql)
            implementation(libs.testcontainers.r2dbc)

            implementation(libs.slf4j.simple)

            runtimeOnly(libs.postgresql.jdbc)
            runtimeOnly(libs.postgresql.r2dbc)
        }
    }
}

tasks {
    val jvmProcessResources by getting
    val fixMissingResources by creating(Copy::class) {
        dependsOn(jvmProcessResources)
        from("${layout.buildDirectory}/processedResources/jvm/main")
        into("${layout.buildDirectory}/classes/kotlin/jvm/main")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    val jvmJar by getting(Jar::class) {
        dependsOn(fixMissingResources)
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}