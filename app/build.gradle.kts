plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "dev.sergey.triad"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.sergey.triad"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations += listOf("en", "ru", "vi")
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
    lint {
        abortOnError = true
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.appcompat)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

fun coverageTree() = fileTree("build/tmp/kotlin-classes/debug") {
    include("dev/sergey/triad/domain/**")
    include("dev/sergey/triad/data/scheduler/**")
    include("dev/sergey/triad/data/content/PackParser*")
    include("dev/sergey/triad/data/content/*Dto*")
    include("dev/sergey/triad/data/backup/**")
    include("dev/sergey/triad/data/translate/**")
    include("dev/sergey/triad/data/db/Mappers*")
}

val coverageExec = layout.buildDirectory.file(
    "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
)

tasks.register<JacocoReport>("jacocoDomainReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(coverageTree())
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(coverageExec)
}

tasks.register<JacocoCoverageVerification>("jacocoVerify") {
    dependsOn("jacocoDomainReport")
    classDirectories.setFrom(coverageTree())
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(coverageExec)
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn("jacocoVerify")
}
