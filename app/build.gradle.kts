import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android") version "2.0.21"
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

android {
    namespace = "project.bizpalm"
    compileSdk = 35

    defaultConfig {
        applicationId = "project.bizpalm"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Disabled minification and resource shrinking to avoid build issues
            isMinifyEnabled = false
            isShrinkResources = false
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

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/native-image/**"
        }
    }

    lint {
        // This completely disables Lint checks during the build process
        // to avoid the Java 25 version parsing crash (25.0.2 error).
        checkReleaseBuilds = false
        abortOnError = false
        checkDependencies = false
        ignoreWarnings = true
        quiet = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.recyclerview)

    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    implementation(libs.mlkit.barcode.scanning)

    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    implementation(libs.itext.core)

    implementation(libs.zxing.core)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.gson)

    implementation(libs.poi.ooxml)

    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    implementation(libs.guava)

    // Advanced Analytics Libraries
    implementation(libs.apache.commons.math)
    implementation(libs.mpandroidchart)
    
    // UI Enhancements
    implementation("io.noties.markwon:core:4.6.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
