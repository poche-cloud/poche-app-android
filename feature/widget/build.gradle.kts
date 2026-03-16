plugins {
    alias(libs.plugins.poche.android.library)
    alias(libs.plugins.poche.android.compose)
    alias(libs.plugins.poche.android.hilt)
}

android {
    namespace = "cloud.poche.feature.widget"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
}
