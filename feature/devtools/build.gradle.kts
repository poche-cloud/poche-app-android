plugins {
    alias(libs.plugins.poche.android.feature)
    alias(libs.plugins.poche.android.testing)
}

android {
    namespace = "cloud.poche.feature.devtools"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.extended)
}
