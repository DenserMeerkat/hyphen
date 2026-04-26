import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeAppWasm")
        browser()
        binaries.executable()
    }

    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(npm("@sqlite.org/sqlite-wasm", "3.45.1-build1"))
                implementation(devNpm("copy-webpack-plugin", "12.0.2"))
            }
        }

        @OptIn(ExperimentalWasmDsl::class)
        val wasmJsMain by getting {
            dependencies {
                implementation(npm("@sqlite.org/sqlite-wasm", "3.45.1-build1"))
                implementation(devNpm("copy-webpack-plugin", "12.0.2"))
            }
        }

        commonMain.dependencies {
            implementation(project(":hyphen"))
            implementation(project(":sample:shared"))

            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
        }
    }
}