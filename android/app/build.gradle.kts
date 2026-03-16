plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    jacoco
}

android {
    namespace = "com.xaymaca.sit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xaymaca.sit"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }

    testOptions {
        unitTests.all {
            it.extensions.configure(JacocoTaskExtension::class) {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }
}

// JaCoCo coverage report task
tasks.register<JacocoReport>("jacocoUnitTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*",
        // Hilt/Dagger generated
        "**/*_HiltModules*", "**/*_Factory*", "**/*_MembersInjector*",
        "**/Hilt_*", "**/*Hilt*",
        // Room generated
        "**/*_Impl*",
        // Compose compiler generated
        "**/*\$\$inlined*", "**/*ComposableSingletons*",
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug/classes") {
        exclude(fileFilter)
    }
    val kotlinDebugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    sourceDirectories.setFrom(files("src/main/java"))
    classDirectories.setFrom(files(debugTree, kotlinDebugTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include("jacoco/testDebugUnitTest.exec", "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    })
}

dependencies {
    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.navigation)
    implementation(libs.activity.compose)

    // Room (local database — equivalent to SwiftData)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt (dependency injection)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Gson (JSON serialization for Room list fields)
    implementation(libs.gson)

    debugImplementation(libs.compose.ui.tooling)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.gson)
}
