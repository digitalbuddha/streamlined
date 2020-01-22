package io.github.reactivecircus.streamlined

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.internal.file.impl.DefaultFileMetadata.file
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

/**
 * Configure root project.
 * Note that classpath dependencies still need to be defined in the `buildscript` block in the top-level build.gradle.kts file.
 */
internal fun Project.configureForRootProject() {
    // register task for cleaning the build directory in the root project
    tasks.register("clean", Delete::class.java) {
        delete(rootProject.buildDir)
    }
}

/**
 * Apply baseline configurations for all projects (including the root project).
 */
internal fun Project.configureForAllProjects() {
    // apply and configure detekt plugin
    configureDetektPlugin()

    repositories.apply {
        mavenCentral()
        google()
        jcenter()
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }
    }

    tasks.withType(JavaCompile::class.java).configureEach {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    tasks.withType(KotlinJvmCompile::class.java).configureEach {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    tasks.withType(KotlinCompile::class.java).configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + additionalCompilerArgs
        }
    }

    tasks.withType(Test::class.java).configureEach {
        maxParallelForks = Runtime.getRuntime().availableProcessors() * 2
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }
}

/**
 * Apply baseline configurations for all Android projects (Application and Library).
 */
internal fun BaseExtension.configureCommonAndroidOptions() {
    setCompileSdkVersion(androidSdk.compileSdk)
    buildToolsVersion(androidSdk.buildTools)

    defaultConfig.apply {
        minSdkVersion(androidSdk.minSdk)
        targetSdkVersion(androidSdk.targetSdk)

        // only support English for now
        resConfigs("en")
    }

    testOptions.animationsDisabled = true

    dexOptions.preDexLibraries = !isCiBuild

    compileOptions(Action {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    })
}

/**
 * Apply configuration options for Android Library projects.
 */
@Suppress("UnstableApiUsage")
internal fun LibraryExtension.configureAndroidLibraryOptions() {
    // Disable generating BuildConfig.java
    buildFeatures.buildConfig = false
}

/**
 * Apply configuration options for Android Application projects.
 */
internal fun AppExtension.configureAndroidApplicationOptions(project: Project) {
    lintOptions {
        disable("ParcelCreator")
        disable("GoogleAppIndexingWarning")
        isQuiet = false
        isIgnoreWarnings = false
        htmlReport = true
        xmlReport = true
        htmlOutput = File("${project.buildDir}/reports/lint/lint-reports.html")
        xmlOutput = File("${project.buildDir}/reports/lint/lint-reports.xml")
        isCheckDependencies = true
        isIgnoreTestSources = true
    }

    packagingOptions {
        excludes = setOf(
            "META-INF/*.kotlin_module",
            "META-INF/proguard/coroutines.pro",
            "META-INF/MANIFEST.MF"
        )
    }
}