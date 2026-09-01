import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    kotlin("jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform") version "2.13.1"
}

group = "com.swiftcmd"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

val isCI = System.getenv("CI") == "true"

dependencies {
    intellijPlatform {
        if (isCI) {
            create(IntelliJPlatformType.IntellijIdea, "2026.1")
        } else {
            local("/Applications/IntelliJ IDEA.app/Contents")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

intellijPlatform {
    instrumentCode = true
    pluginConfiguration {
        ideaVersion {
            // 231 = IntelliJ IDEA 2023.1. The platform Gradle plugin derives
            // since-build from the target platform (261 = 2026.1) when not set
            // explicitly, which is why 2023.1 rejected the plugin with
            // "version not supported" at install time.
            sinceBuild = "231"
            untilBuild = "263.*"
        }
    }
    pluginVerification {
        ides {
            create(IntelliJPlatformType.IntellijIdea, "2023.1.5")
        }
    }
}
