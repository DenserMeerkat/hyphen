pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Hyphen"

include(":hyphen-core")
include(":hyphen-inline")
include(":hyphen")
include(":hyphen-blocks")
include(":sample:shared")
include(":sample:android")
include(":sample:desktop")
include(":sample:web")
