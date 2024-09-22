plugins {
    alias(libs.plugins.kotlinJvm)
}

group = "com.earthrevealed.immaru"
version = "1.0.0"

dependencies {
    implementation(projects.core)

    implementation(libs.microutils.kotlin.logging.jvm)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.apache.tika.core)

    testImplementation(libs.kotlin.test.junit)
}
