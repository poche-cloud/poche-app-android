package cloud.poche.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

internal fun Project.configureAndroidCompose() {
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

    extensions.findByType<ApplicationExtension>()?.apply {
        buildFeatures { compose = true }
    }
    extensions.findByType<LibraryExtension>()?.apply {
        buildFeatures { compose = true }
    }

    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))
        add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
    }
}
