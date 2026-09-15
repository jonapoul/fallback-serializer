@file:Suppress("UnstableApiUsage")

import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.LIBRARY
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.Companion.attribute
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  alias(libs.plugins.kotlin.jvm)
  id("fallback.convention")
}

kotlin {
  compilerOptions {
    optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
  }
}

// Libraries on the classpath of the code compiled by each test
val testLibraries = configurations.dependencyScope("testLibraries")

val testLibrariesClasspath =
  configurations.resolvable("testLibrariesClasspath") {
    extendsFrom(testLibraries)
    // The test framework supplies the Kotlin stdlib itself
    exclude(group = "org.jetbrains.kotlin")
    attributes {
      attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
      attribute(USAGE_ATTRIBUTE, objects.named(JAVA_RUNTIME))
      attribute(attribute, jvm)
    }
  }

dependencies {
  testImplementation(project(":compiler"))
  testImplementation(kotlin("compiler"))
  testImplementation(kotlin("compiler-internal-test-framework"))
  testImplementation(kotlin("serialization-compiler-plugin"))
  testImplementation(kotlin("stdlib"))
  testImplementation(kotlin("test-junit5"))

  testLibraries(project(":runtime"))
  testLibraries(libs.kotlinx.serialization.json)

  testRuntimeOnly(libs.junit4)
  testRuntimeOnly(kotlin("annotations-jvm"))
  testRuntimeOnly(kotlin("reflect"))
  testRuntimeOnly(kotlin("script-runtime"))
  testRuntimeOnly(kotlin("test"))
}

val testData = layout.projectDirectory.dir("src/test/data")

val generateTests =
  tasks.register<JavaExec>("generateTests") {
    description = "Generates the JUnit suites in src/test/java from the files in src/test/data"
    inputs.dir(testData).withPropertyName("testData").withPathSensitivity(RELATIVE)
    outputs.dir(layout.projectDirectory.dir("src/test/java")).withPropertyName("generatedTests")

    // Only the Kotlin test classes, so the old generated suites don't need compiling first
    classpath =
      files(
        tasks
          .named<KotlinJvmCompile>("compileTestKotlin")
          .flatMap(KotlinJvmCompile::destinationDirectory),
        configurations.testRuntimeClasspath,
      )
    mainClass = "fallback.serializer.compiler.GenerateTestsKt"
    workingDir = rootDir
  }

tasks.named<JavaCompile>("compileTestJava") { mustRunAfter(generateTests) }

tasks.test {
  inputs.dir(testData).withPropertyName("testData").withPathSensitivity(RELATIVE)
  workingDir = rootDir
  maxHeapSize = "2g"

  // Pass -PupdateTestData to rewrite the expected diagnostics in src/test/data
  if (providers.gradleProperty("updateTestData").isPresent) {
    systemProperty("kotlin.test.update.test.data", "true")
    outputs.upToDateWhen { false }
  }

  systemProperty("idea.ignore.disabled.plugins", "true")
  systemProperty("idea.home.path", rootDir.absolutePath)

  classpathProperty(testLibrariesClasspath.get(), "fallback.testLibraries")

  val runtimeClasspath = configurations.testRuntimeClasspath.get()
  kotlinLibraryProperty(runtimeClasspath, "kotlin-stdlib")
  kotlinLibraryProperty(runtimeClasspath, "kotlin-stdlib-jdk8", "kotlin.full.stdlib.path")
  kotlinLibraryProperty(runtimeClasspath, "kotlin-reflect", "kotlin.reflect.jar.path")
  kotlinLibraryProperty(runtimeClasspath, "kotlin-test")
  kotlinLibraryProperty(runtimeClasspath, "kotlin-script-runtime")
  kotlinLibraryProperty(
    runtimeClasspath,
    "kotlin-annotations-jvm",
    "kotlin.mockJDK.annotations.path",
  )
}

abstract class ClasspathArgumentProvider : CommandLineArgumentProvider {
  @get:Input abstract val names: ListProperty<String>
  @get:Classpath abstract val classpath: ConfigurableFileCollection

  override fun asArguments(): Iterable<String> {
    if (classpath.isEmpty) return emptyList()
    return names.get().map { "-D$it=${classpath.asPath}" }
  }
}

fun Test.classpathProperty(files: FileCollection, vararg names: String) {
  jvmArgumentProviders.add(
    objects.newInstance<ClasspathArgumentProvider>().apply {
      this.names.addAll(*names)
      classpath.from(files)
    }
  )
}

// Passes a Kotlin library jar to the test framework under its standard property name, plus aliases
fun Test.kotlinLibraryProperty(classpath: Configuration, jarName: String, vararg aliases: String) {
  val regex = Regex("$jarName-\\d.*\\.jar")
  // Filtered lazily, resolving the configuration here breaks KGP's dependency constraints
  val jar = classpath.filter { regex.matches(it.name) }
  classpathProperty(jar, "org.jetbrains.kotlin.test.$jarName", *aliases)
}
