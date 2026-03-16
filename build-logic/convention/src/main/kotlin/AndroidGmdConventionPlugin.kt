import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get

class AndroidGmdConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.withPlugin("com.android.application") {
                extensions.configure<ApplicationExtension> {
                    @Suppress("UnstableApiUsage")
                    testOptions.managedDevices.localDevices.apply {
                        maybeCreate("pixel2Api34").apply {
                            device = "Pixel 2"
                            apiLevel = 34
                            systemImageSource = "aosp-atd"
                        }
                    }
                }
            }
        }
    }
}
