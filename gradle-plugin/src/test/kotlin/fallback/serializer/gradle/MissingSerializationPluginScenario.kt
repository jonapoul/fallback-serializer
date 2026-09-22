package fallback.serializer.gradle

import blueprint.test.assertThatTask
import blueprint.test.buildGradleKts
import blueprint.test.buildsSuccessfully
import blueprint.test.outputContains
import kotlin.test.Test

class MissingSerializationPluginScenario : FallbackScenarioTest() {
  override val fileTree = fileTree {
    settings()

    buildGradleKts(
      """
      plugins {
        kotlin("jvm")
        id("$PLUGIN_ID")
      }
      """
        .trimIndent()
    )
  }

  @Test
  fun `warns when the serialization plugin isn't applied`() = runScenario {
    assertThatTask(":help")
      .buildsSuccessfully()
      .outputContains(
        "Fallback Serializer: root project 'test-project' doesn't apply " +
          "org.jetbrains.kotlin.plugin.serialization, so the generated serializers won't be used"
      )
  }
}
