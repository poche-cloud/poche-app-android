plugins {
    alias(libs.plugins.poche.android.library)
    alias(libs.plugins.poche.android.hilt)
}

android {
    namespace = "cloud.poche.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
}
