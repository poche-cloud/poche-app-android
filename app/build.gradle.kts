plugins {
    alias(libs.plugins.poche.android.application)
    alias(libs.plugins.poche.android.compose)
    alias(libs.plugins.poche.android.hilt)
    alias(libs.plugins.poche.android.gmd)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "cloud.poche.app"

    val releaseKeystorePath = System.getenv("KEYSTORE_PATH")
    val releaseKeystorePassword = System.getenv("KEYSTORE_PASSWORD")
    val releaseKeyAlias = System.getenv("KEY_ALIAS")
    val releaseKeyPassword = System.getenv("KEY_PASSWORD")
    val hasReleaseSigningConfig =
        listOf(
            releaseKeystorePath,
            releaseKeystorePassword,
            releaseKeyAlias,
            releaseKeyPassword,
        ).all { !it.isNullOrBlank() }
    val releaseSigningRequiredTasks =
        setOf(
            "assemble",
            "assembleprod",
            "assembleprodrelease",
            "assemblerelease",
            "assemblestg",
            "assemblestgrelease",
            "build",
            "bundle",
            "bundleprod",
            "bundleprodrelease",
            "bundlerelease",
            "bundlestg",
            "bundlestgrelease",
        )
    val requiresReleaseSigningConfig =
        gradle.startParameter.taskNames
            .map { it.substringAfterLast(":").lowercase() }
            .any { taskName ->
                taskName.contains("prodrelease") ||
                    taskName.contains("stgrelease") ||
                    taskName in releaseSigningRequiredTasks
            }

    if (requiresReleaseSigningConfig && !hasReleaseSigningConfig) {
        error(
            "KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, and KEY_PASSWORD are required " +
                "for staging and production release builds.",
        )
    }

    defaultConfig {
        applicationId = "cloud.poche.app"
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigningConfig) {
            create("release") {
                storeFile = file(checkNotNull(releaseKeystorePath))
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            if (!hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("debug")
            }
        }
        create("stg") {
            dimension = "environment"
            applicationIdSuffix = ".stg"
            versionNameSuffix = "-stg"
        }
        create("prod") {
            dimension = "environment"
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:analytics"))
    implementation(project(":core:auth"))
    implementation(project(":core:notifications"))
    implementation(project(":core:remote-config"))
    implementation(project(":core:ui"))

    // Feature modules
    implementation(project(":feature:home"))
    implementation(project(":feature:capture"))
    implementation(project(":feature:memo"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:devtools"))
    implementation(project(":feature:widget"))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.navigation.compose)

    // Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.config)

    // Testing
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
