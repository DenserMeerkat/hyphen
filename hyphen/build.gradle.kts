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
}

group = "io.github.densermeerkat"
version = "0.3.0-alpha01"

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

publishing {
    publications.withType<MavenPublication> {
        val pubName = name
        val javadocJarTask = project.tasks.register("javadocJar${pubName.replaceFirstChar { it.uppercaseChar() }}", Jar::class) {
            dependsOn(project.tasks.named("dokkaGenerate"))
            archiveClassifier.set("javadoc")
            from(project.layout.buildDirectory.dir("dokka/html"))
            destinationDirectory.set(project.layout.buildDirectory.dir("libs/javadoc/$pubName"))
        }

        artifact(javadocJarTask)

        pom {
            name.set("Hyphen")
            description.set("A lightweight WYSIWYG Markdown editor for Compose Multiplatform (KMP).")
            url.set("https://github.com/DenserMeerkat/hyphen")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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

    repositories {
        maven {
            name = "LocalTest"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    sign(publishing.publications)
}