plugins {
    alias(libs.plugins.poche.android.feature)
}

android {
    namespace = "cloud.poche.feature.devtools"
}

dependencies {
    implementation(libs.androidx.compose.material.icons.extended)
}
