plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.denser.hyphen.sample.shared"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) }
    }
    jvm("desktop") {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":hyphen"))

                implementation(libs.runtime)
                implementation(libs.foundation)
                implementation(libs.material3)
                implementation(libs.ui)
                implementation(libs.components.resources)
            }
        }
    }
}