plugins {
    alias(libs.plugins.poche.android.library)
    alias(libs.plugins.poche.android.hilt)
}

android {
    namespace = "cloud.poche.core.testing"
}

dependencies {
    api(libs.junit5.api)
    api(libs.junit5.params)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(libs.mockk)
    api(libs.hilt.android.testing)

    runtimeOnly(libs.junit5.engine)

    implementation(project(":core:common"))
    implementation(project(":core:model"))
}
