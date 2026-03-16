plugins {
    alias(libs.plugins.poche.android.library)
    alias(libs.plugins.poche.android.hilt)
    alias(libs.plugins.poche.android.testing)
}

android {
    namespace = "cloud.poche.core.domain"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
}
