package fallback.serializer.gradle

import blueprint.test.assertThatTask
import blueprint.test.buildGradleKts
import blueprint.test.failsBuild
import blueprint.test.outputContains
import blueprint.test.taskFailed
import kotlin.test.Test

// The runtime added by hand, without our Gradle plugin
class MissingFallbackPluginScenario : FallbackScenarioTest() {
  override val fileTree = fileTree {
    settings()

    buildGradleKts(
      """
      plugins {
        kotlin("jvm")
        kotlin("plugin.serialization")
      }

      dependencies {
        implementation("$GROUP:runtime:$VERSION")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$KOTLINX_SERIALIZATION_VERSION")
      }
      """
        .trimIndent()
    )

    "src/main/kotlin/LogLevel.kt"(
      """
      import fallback.serializer.Fallback
      import kotlinx.serialization.Serializable

      @Serializable
      enum class LogLevel {
        Info,
        @Fallback Unrecognised,
      }
      """
        .trimIndent()
    )
  }

  @Test
  fun `fails to compile @Fallback without the plugin`() = runScenario {
    assertThatTask(":compileKotlin")
      .failsBuild()
      .taskFailed(":compileKotlin")
      .outputContains(
        "@Fallback does nothing without the compiler plugin. Apply the " +
          "dev.jonpoulton.fallbackserializer Gradle plugin."
      )
  }
}
