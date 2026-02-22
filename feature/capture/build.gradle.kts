plugins {
    alias(libs.plugins.poche.android.feature)
    alias(libs.plugins.poche.android.testing)
}

android {
    namespace = "cloud.poche.feature.capture"
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.androidx.compose.material.icons.extended)
}
