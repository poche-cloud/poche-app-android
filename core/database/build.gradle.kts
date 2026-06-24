plugins {
    alias(libs.plugins.poche.android.library)
    alias(libs.plugins.poche.android.hilt)
    alias(libs.plugins.poche.android.room)
}

android {
    namespace = "cloud.poche.core.database"
}

dependencies {
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.turbine)
}
