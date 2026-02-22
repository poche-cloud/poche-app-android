plugins {
    alias(libs.plugins.poche.android.library)
    alias(libs.plugins.poche.android.compose)
}

android {
    namespace = "cloud.poche.core.ui"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))

    implementation(libs.androidx.compose.ui)
    implementation(libs.coil.compose)
}
