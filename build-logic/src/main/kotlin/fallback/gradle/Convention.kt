@file:OptIn(ExperimentalAbiValidation::class)
@file:Suppress("UnstableApiUsage")

package fallback.gradle

import app.cash.licensee.LicenseeExtension
import app.cash.licensee.LicenseePlugin
import app.cash.licensee.UnusedAction
import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.plugin.DetektPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.testing.internal.KotlinTestReport
import straitjacket.StraitjacketPlugin

class Convention : Plugin<Project> {
  override fun apply(target: Project) {
    // Matches the published coordinates, so a build that includes this one (like sample) gets these
    // projects in place of the dependencies the Gradle plugin adds
    target.group = target.providers.gradleProperty("GROUP").get()
    target.version = target.providers.gradleProperty("VERSION_NAME").get()

    listOf("org.jetbrains.kotlin.jvm", "org.jetbrains.kotlin.multiplatform").forEach { id ->
      target.pluginManager.withPlugin(id) { target.configureKotlin() }
    }

    target.configureDetekt()
    target.configureLicensee()
    target.configureStraitjacket()
    target.configureTests()
    target.configureOtherChecks()
    target.configurePublishing()
  }

  private fun Project.configureKotlin() {
    val javaFile = isolated.rootProject.projectDirectory.file(".java-version")
    val jdkVersion = providers.fileContents(javaFile).asText.map { it.trim().toInt() }

    extensions.configure(KotlinBaseExtension::class.java) { e ->
      e.jvmToolchain(jdkVersion.get())
    }

    extensions.configure(HasConfigurableKotlinCompilerOptions::class.java) { e ->
      e.compilerOptions {
        allWarningsAsErrors.set(true)
        freeCompilerArgs.addAll("-Xcontext-sensitive-resolution")
      }
    }

    tasks.register("compileAll") { t ->
      t.dependsOn(tasks.withType(KotlinCompilationTask::class.java))
    }

    tasks.withType(Test::class.java).configureEach { t -> t.useJUnitPlatform() }
  }

  private fun Project.configureDetekt() {
    pluginManager.apply(DetektPlugin::class.java)

    extensions.configure(DetektExtension::class.java) { e ->
      e.config.from(isolated.rootProject.projectDirectory.file("config/detekt.yml"))
      e.baseline.set(file("detekt-baseline.xml"))
      e.buildUponDefaultConfig.set(true)
      e.allRules.set(true)
      e.parallel.set(true)
      e.debug.set(false)
    }

    val detektTasks = tasks.withType(Detekt::class.java)

    tasks.register("detektCheck") { t ->
      t.group = VERIFICATION_GROUP
      t.dependsOn(detektTasks)
    }

    detektTasks.configureEach { t ->
      t.reports { r ->
        r.html.required.set(true)
        r.sarif.required.set(false)
        r.checkstyle.required.set(true)
        r.markdown.required.set(false)
      }

      // Skip buildconfig output
      t.exclude { node ->
        !node.isDirectory && node.file.absolutePath.contains("generated", ignoreCase = true)
      }
    }
  }

  private fun Project.configureLicensee() {
    pluginManager.apply(LicenseePlugin::class.java)

    extensions.configure(LicenseeExtension::class.java) { e ->
      e.allow("Apache-2.0")
      e.unusedAction(UnusedAction.IGNORE)
    }
  }

  private fun Project.configureStraitjacket() {
    pluginManager.apply(StraitjacketPlugin::class.java)
  }

  private fun Project.configureTests() {
    tasks.register("testAll") { t ->
      t.group = VERIFICATION_GROUP
      t.dependsOn(tasks.withType(Test::class.java))
      t.dependsOn(tasks.withType(KotlinTestReport::class.java))
    }
  }

  private fun Project.configureOtherChecks() {
    if (providers.gradleProperty("otherChecks").isPresent) {
      listOf(AbstractTestTask::class, KotlinTestReport::class, Detekt::class).forEach { klass ->
        tasks.withType(klass.java).configureEach { t -> t.onlyIf { false } }
      }
    }
  }

  private fun Project.configurePublishing() {
    pluginManager.withPlugin("com.vanniktech.maven.publish") {
      logger.lifecycle("configurePublishing publish $path")
      extensions.configure(KotlinProjectExtension::class.java) { e ->
        e.explicitApi()
        e.abiValidation()
      }

      val publishing = extensions.getByType(PublishingExtension::class.java)
      extensions.configure(SigningExtension::class.java) { e ->
        e.sign(publishing.publications)
      }

      pluginManager.withPlugin("base") {
        logger.lifecycle("configurePublishing base $path")
        val checkSigning = tasks.named("checkSigningConfiguration")
        tasks.named("check") { t -> t.dependsOn(checkSigning) }
      }
    }
  }
}
