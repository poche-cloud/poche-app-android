plugins {
    alias(libs.plugins.poche.android.library)
    alias(libs.plugins.poche.android.hilt)
}

android {
    namespace = "cloud.poche.core.datastore"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.datastore.preferences)
    implementation(libs.google.tink.android)
}
