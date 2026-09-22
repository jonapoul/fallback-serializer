package fallback.serializer.gradle

import blueprint.test.assertThatTask
import blueprint.test.buildGradleKts
import blueprint.test.failsBuild
import blueprint.test.outputContains
import kotlin.test.Test
import org.gradle.testkit.runner.GradleRunner

class KotlinVersionScenario : FallbackScenarioTest() {
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

  override fun defaultRunner(): GradleRunner =
    super.defaultRunner().withPluginClasspath(oldKotlinClasspath)

  @Test
  fun `fails with an unsupported Kotlin version`() = runScenario {
    assertThatTask(":help")
      .failsBuild()
      .outputContains(
        "Fallback Serializer $VERSION needs Kotlin ${KOTLIN_VERSIONS.joinToString()}, but root " +
          "project 'test-project' uses Kotlin 2.3.21"
      )
  }
}
