import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Automatic Version Management (Major.Minor.Patch.Build format: e.g. 1.0.0.3)
val versionPropsFile = file("version.properties")
val versionProps = Properties().apply {
    if (versionPropsFile.exists()) {
        versionPropsFile.inputStream().use { load(it) }
    } else {
        setProperty("versionMajor", "1")
        setProperty("versionMinor", "0")
        setProperty("versionPatch", "0")
        setProperty("versionBuild", "1")
        setProperty("versionCode", "1")
        versionPropsFile.outputStream().use { store(it, "Version Configuration") }
    }
}

val vMajor = versionProps.getProperty("versionMajor", "1").toIntOrNull() ?: 1
val vMinor = versionProps.getProperty("versionMinor", "0").toIntOrNull() ?: 0
val vPatch = versionProps.getProperty("versionPatch", "0").toIntOrNull() ?: 0
val baseBuild = versionProps.getProperty("versionBuild", "1").toIntOrNull() ?: 1
val baseCode = versionProps.getProperty("versionCode", "1").toIntOrNull() ?: 1

// Check for CI/CD environment (GitHub Actions run number)
val githubRunNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()

val vBuild = when {
    githubRunNumber != null -> githubRunNumber
    else -> baseBuild
}

val vCode = when {
    githubRunNumber != null -> 1000 + githubRunNumber
    else -> baseCode
}

val appVersionName = "$vMajor.$vMinor.$vPatch.$vBuild"
val appVersionCode = vCode

android {
    namespace = "com.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aistudio.investmenttracker.kxcipu"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("debugConfig") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debugConfig")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debugConfig")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.okhttp)
    implementation(libs.gson)
}

// Auto-increment build counter in version.properties after assemble/bundle finishes
tasks.register("incrementVersionBuild") {
    description = "Increments build number in version.properties for subsequent builds"
    onlyIf { githubRunNumber == null }
    doLast {
        val currentBuild = versionProps.getProperty("versionBuild", "68").toIntOrNull() ?: 68
        val currentCode = versionProps.getProperty("versionCode", "1068").toIntOrNull() ?: 1068
        versionProps.setProperty("versionBuild", (currentBuild + 1).toString())
        versionProps.setProperty("versionCode", (currentCode + 1).toString())
        versionPropsFile.outputStream().use {
            versionProps.store(it, "Automatically updated by build")
        }
        println("Prepared next build version: $vMajor.$vMinor.$vPatch.${currentBuild + 1} (code: ${currentCode + 1})")
    }
}

tasks.matching { it.name.startsWith("assemble") || it.name.startsWith("bundle") }.configureEach {
    finalizedBy("incrementVersionBuild")
}

