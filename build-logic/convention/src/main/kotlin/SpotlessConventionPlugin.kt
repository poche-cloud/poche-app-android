import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class SpotlessConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.diffplug.spotless")

            extensions.configure<SpotlessExtension> {
                kotlin {
                    target("src/**/*.kt")
                    targetExclude("**/build/**/*.kt")
                    ktlint()
                        .editorConfigOverride(
                            mapOf(
                                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
                                "max_line_length" to "120",
                                "indent_size" to "4",
                                "insert_final_newline" to "true",
                            ),
                        )
                }
                kotlinGradle {
                    target("*.gradle.kts")
                    ktlint()
                }
            }
        }
    }
}
