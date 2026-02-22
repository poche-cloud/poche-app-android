plugins {
    alias(libs.plugins.poche.android.feature)
    alias(libs.plugins.poche.android.testing)
}

android {
    namespace = "cloud.poche.feature.memo"
}

dependencies {
    implementation(project(":core:data"))
}
