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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // StickyScrollView for Sticky Header
        // https://github.com/amarjain07/StickyScrollView
        maven { url = uri("https://www.jitpack.io" )  }
    }
}

rootProject.name = "BottomNavigation"
include(":app")
 