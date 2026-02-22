plugins {
    alias(libs.plugins.poche.android.feature)
    alias(libs.plugins.poche.android.testing)
}

android {
    namespace = "cloud.poche.feature.settings"
}

dependencies {
    implementation(project(":core:auth"))
    implementation(project(":core:data"))
    implementation(libs.androidx.compose.material.icons.extended)
}
