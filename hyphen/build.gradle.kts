import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    id("maven-publish")
    id("org.jetbrains.dokka") version "2.1.0"
    signing
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.densermeerkat"
val overrideVersion = project.findProperty("version")?.toString()
version = System.getenv("LIBRARY_VERSION")
    ?: if (!overrideVersion.isNullOrEmpty() && overrideVersion != "unspecified") overrideVersion else null
    ?: "0.5.0-alpha02"

android {
    namespace = "com.denser.hyphen"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.resources {
    packageOfResClass = "com.denser.hyphen"
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("hyphen")
        browser()
    }

    js(IR) {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.runtime)
                implementation(libs.foundation)
                implementation(libs.material3)
                implementation(libs.ui)
                implementation(libs.components.resources)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.material)
                compileOnly(libs.androidx.compose.ui.tooling.preview)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

if (!project.hasProperty("mavenCentralUsername")) {
    val localUsername = project.findProperty("sonatypeUsername")?.toString()
        ?: project.findProperty("ossrhUsername")?.toString()
    if (!localUsername.isNullOrEmpty()) {
        extra.set("mavenCentralUsername", localUsername)
    }
}

if (!project.hasProperty("mavenCentralPassword")) {
    val localPassword = project.findProperty("sonatypePassword")?.toString()
        ?: project.findProperty("ossrhPassword")?.toString()
    if (!localPassword.isNullOrEmpty()) {
        extra.set("mavenCentralPassword", localPassword)
    }
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = group.toString(),
        artifactId = "hyphen",
        version = version.toString()
    )

    pom {
        name.set("Hyphen")
        description.set("A lightweight WYSIWYG Markdown editor for Compose Multiplatform (KMP).")
        url.set("https://github.com/DenserMeerkat/hyphen")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("DenserMeerkat")
                name.set("DenserMeerkat")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/DenserMeerkat/hyphen.git")
            developerConnection.set("scm:git:ssh://github.com/DenserMeerkat/hyphen.git")
            url.set("https://github.com/DenserMeerkat/hyphen")
        }
    }
}