import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
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

room3 {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) }
    }
    jvm("desktop") {
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    js(IR) {
        browser()
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
                implementation(libs.androidx.room3.runtime)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.sqlite.bundled)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.androidx.sqlite.bundled)
            }
        }

        @OptIn(ExperimentalWasmDsl::class)
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.androidx.sqlite.web)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.androidx.sqlite.web)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room3.compiler)
    add("kspDesktop", libs.androidx.room3.compiler)
    add("kspWasmJs", libs.androidx.room3.compiler)
    add("kspJs", libs.androidx.room3.compiler)
}