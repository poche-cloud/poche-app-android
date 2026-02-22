plugins {
    alias(libs.plugins.poche.android.library)
    alias(libs.plugins.poche.android.compose)
}

android {
    namespace = "cloud.poche.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)

    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
}
