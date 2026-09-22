package fallback.serializer.gradle

import blueprint.test.FileTree
import blueprint.test.ScenarioTest
import blueprint.test.settingsGradleKts
import java.io.File

// Folder of compiler plugin and runtime jars, standing in for Maven Central
private val testRepo: String =
  File(checkNotNull(System.getProperty("fallback.testRepo"))).invariantSeparatorsPath

// Our plugin with an older Kotlin Gradle plugin
internal val oldKotlinClasspath: List<File> =
  checkNotNull(System.getProperty("fallback.oldKotlinClasspath"))
    .split(File.pathSeparator)
    .map(::File)

abstract class FallbackScenarioTest : ScenarioTest() {
  override val gradleVersion = GRADLE_VERSION

  protected fun FileTree.Builder.settings() {
    settingsGradleKts(
      """
      rootProject.name = "test-project"

      pluginManagement {
        repositories {
          mavenCentral()
          gradlePluginPortal()
        }
      }

      dependencyResolutionManagement {
        repositories {
          exclusiveContent {
            forRepository { flatDir { dirs("$testRepo") } }
            filter { includeGroup("$GROUP") }
          }
          mavenCentral()
        }
      }
      """
        .trimIndent()
    )
  }
}
