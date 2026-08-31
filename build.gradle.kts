import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    kotlin("jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform") version "2.18.1"
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
            create(IntelliJPlatformType.IntellijIdeaCommunity, "2023.1")
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
}
