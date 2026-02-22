plugins {
    alias(libs.plugins.poche.android.library)
    alias(libs.plugins.poche.android.hilt)
    alias(libs.plugins.poche.android.room)
}

android {
    namespace = "cloud.poche.core.database"
}
