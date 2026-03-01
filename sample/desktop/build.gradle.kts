plugins {
    kotlin("jvm")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

dependencies {
    implementation(project(":hyphen"))
    implementation(project(":sample:shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.material3)
    implementation(libs.foundation)
    implementation(libs.ui)
}

compose.desktop {
    application {
        mainClass = "com.denser.hyphen.sample.desktop.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "HyphenDesktopSample"
            macOS {
                bundleID = "com.denser.hyphen.sample.desktop"
            }
        }
    }
}