package fallback.serializer.gradle

import blueprint.test.assertThatTask
import blueprint.test.buildGradleKts
import blueprint.test.buildsSuccessfully
import blueprint.test.outputContains
import blueprint.test.outputDoesNotContain
import blueprint.test.withGradleProperty
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
  fun `warns with an unsupported Kotlin version`() = runScenario {
    assertThatTask(":help")
      .buildsSuccessfully()
      .outputContains(
        "Fallback Serializer $VERSION is tested against Kotlin ${KOTLIN_VERSIONS.joinToString()}, " +
          "but root project 'test-project' uses Kotlin 2.3.21. Set " +
          "fallback.skipKotlinVersionCheck=true to hide this warning"
      )
  }

  @Test
  fun `doesn't warn when the version check is skipped`() = runScenario {
    assertThatTask(":help")
      .withGradleProperty("fallback.skipKotlinVersionCheck", true)
      .buildsSuccessfully()
      .outputDoesNotContain("is tested against Kotlin")
  }
}
