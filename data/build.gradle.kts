import io.github.reactivecircus.streamlined.libraries

plugins {
    `streamlined-plugin`
    `core-library-desugaring`
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":remote-base"))
    api(project(":persistence"))

    // Kotlin stdlib
    implementation(libraries.kotlinStdlib)

    // process lifecycle
    implementation(libraries.androidx.lifecycle.process)
    implementation(libraries.androidx.lifecycle.runtimeKtx)

    // Coroutines
    implementation(libraries.kotlinx.coroutines.core)

    // Store
    implementation(libraries.store)

    // Dagger
    implementation(libraries.dagger.runtime)
    kapt(libraries.dagger.compiler)

    // timber
    implementation(libraries.timber)

    // Unit tests
    testImplementation(libraries.junit)
    testImplementation(libraries.mockk)
    testImplementation(libraries.truth)
    testImplementation(project(":coroutines-test-ext"))
}
