package fallback.serializer.gradle

import blueprint.test.assertThatTask
import blueprint.test.buildGradleKts
import blueprint.test.buildsSuccessfully
import blueprint.test.outputContains
import blueprint.test.outputDoesNotContain
import kotlin.test.Test

class JvmProjectScenario : FallbackScenarioTest() {
  override val fileTree = fileTree {
    settings()

    buildGradleKts(
      """
      plugins {
        kotlin("jvm")
        kotlin("plugin.serialization")
        id("$PLUGIN_ID")
        application
      }

      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$KOTLINX_SERIALIZATION_VERSION")
      }

      application {
        mainClass = "MainKt"
      }
      """
        .trimIndent()
    )

    "src/main/kotlin/Main.kt"(
      """
      import fallback.serializer.Fallback
      import kotlinx.serialization.Serializable
      import kotlinx.serialization.json.Json

      @Serializable
      enum class Fruit {
        Apple,
        @Fallback Unknown,
      }

      fun main() {
        println("Decoded: " + Json.decodeFromString<Fruit>("\"Orange\""))
      }
      """
        .trimIndent()
    )
  }

  // Also checks the runtime dependency is added, since Main.kt uses @Fallback
  @Test
  fun `compiles and uses the generated serializer`() = runScenario {
    assertThatTask(":run")
      .buildsSuccessfully()
      .outputContains("Decoded: Unknown")
      .outputDoesNotContain("generated serializers won't be used")
  }
}
