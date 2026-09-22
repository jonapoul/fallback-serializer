import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.LIBRARY
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.Companion.attribute
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.blueprint.test)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.publish)
  id("fallback.convention")
}

buildConfig {
  packageName("fallback.serializer.gradle")
  useKotlinOutput {
    internalVisibility = true
    topLevelConstants = true
  }
  buildConfigField("GROUP", providers.gradleProperty("GROUP"))
  buildConfigField("KOTLIN_VERSION", libs.versions.kotlin)
  buildConfigField("PLUGIN_ID", providers.gradleProperty("PLUGIN_ID"))
  buildConfigField("VERSION", providers.gradleProperty("VERSION_NAME"))

  sourceSets.named("test") {
    packageName("fallback.serializer.gradle")
    useKotlinOutput { topLevelConstants = true }
    buildConfigField("GRADLE_VERSION", GradleVersion.current().version)
    buildConfigField("KOTLINX_SERIALIZATION_VERSION", libs.versions.kotlinx.serialization)
  }
}

// The compiler plugin and runtime jars, which test projects read from a flatDir repository since
// they aren't published
val testRepoJars = configurations.dependencyScope("testRepoJars")

val testRepoClasspath =
  configurations.resolvable("testRepoClasspath") {
    extendsFrom(testRepoJars.get())
    isTransitive = false
    attributes {
      attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
      attribute(USAGE_ATTRIBUTE, objects.named(JAVA_RUNTIME))
      attribute(attribute, jvm)
    }
  }

// A Kotlin Gradle plugin older than the one this plugin is built against
val oldKotlinPluginClasspath =
  configurations.register("oldKotlinPluginClasspath") {
    isCanBeResolved = true
    isCanBeConsumed = false
  }

dependencies {
  compileOnly(kotlin("gradle-plugin-api"))
  compileOnly(kotlin("stdlib"))

  testCompileOnly(libs.junit.api)
  testImplementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))
  testImplementation(libs.assertk)
  testImplementation(libs.blueprint.assertk)
  testPluginClasspath(kotlin("gradle-plugin"))
  testPluginClasspath(kotlin("serialization"))
  testRuntimeOnly(libs.junit.launcher)

  testRepoJars(project(":compiler"))
  testRepoJars(project(":runtime"))

  oldKotlinPluginClasspath(kotlin("gradle-plugin", "2.4.0"))
}

val testRepo =
  tasks.register<Sync>("testRepo") {
    from(testRepoClasspath)
    into(layout.buildDirectory.dir("test-repo"))
    // flatDir finds jars by name and version, and the runtime's JVM jar has a -jvm suffix
    rename("runtime-jvm-", "runtime-")
  }

tasks.test {
  val testRepoDir = layout.buildDirectory.dir("test-repo")
  inputs.files(testRepo).withPropertyName("testRepo").withPathSensitivity(RELATIVE)

  // Our plugin with the old Kotlin Gradle plugin, in place of the usual plugin classpath
  val oldKotlinClasspath = files(sourceSets.main.map { it.output }, oldKotlinPluginClasspath)
  inputs
    .files(oldKotlinClasspath)
    .withPropertyName("oldKotlinClasspath")
    .withNormalizer(ClasspathNormalizer::class)

  jvmArgumentProviders.add {
    listOf(
      "-Dfallback.testRepo=${testRepoDir.get().asFile.absolutePath}",
      "-Dfallback.oldKotlinClasspath=${oldKotlinClasspath.asPath}",
    )
  }
}

tasks.validatePlugins {
  enableStricterValidation = true
  failOnWarning = true
}

gradlePlugin {
  plugins {
    register("fallbackSerializer") {
      id = providers.gradleProperty("PLUGIN_ID").get()
      implementationClass = "fallback.serializer.gradle.FallbackSerializerGradlePlugin"
    }
  }
}
