plugins {
    alias(libs.plugins.poche.android.library)
    alias(libs.plugins.poche.android.hilt)
}

android {
    namespace = "cloud.poche.core.auth"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.android)
}
