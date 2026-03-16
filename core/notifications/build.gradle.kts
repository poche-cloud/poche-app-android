plugins {
    alias(libs.plugins.poche.android.library)
    alias(libs.plugins.poche.android.hilt)
}

android {
    namespace = "cloud.poche.core.notifications"
}

dependencies {
    implementation(project(":core:datastore"))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.core.ktx)
}
