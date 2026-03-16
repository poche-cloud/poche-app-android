import cloud.poche.convention.configureKotlinJvm
import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("poche.android.detekt")
            pluginManager.apply("poche.android.spotless")
            configureKotlinJvm()
        }
    }
}
