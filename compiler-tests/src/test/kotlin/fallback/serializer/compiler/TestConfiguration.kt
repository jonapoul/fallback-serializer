package fallback.serializer.compiler

import java.io.File
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.RuntimeClasspathProvider
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlinx.serialization.compiler.extensions.SerializationLoweringExtension
import org.jetbrains.kotlinx.serialization.compiler.fir.FirSerializationExtensionRegistrar

// Our runtime plus kotlinx.serialization, passed in by the Gradle test task
private val testLibraries: List<File> =
  System.getProperty("fallback.testLibraries")?.split(File.pathSeparator)?.map(::File)
    ?: error("Missing 'fallback.testLibraries' system property")

fun TestConfigurationBuilder.configurePlugin() {
  useConfigurators(::PluginRegistrarConfigurator, ::TestLibrariesConfigurator)
  useCustomRuntimeClasspathProviders(::TestLibrariesClasspathProvider)
}

// Registers our plugin alongside kotlinx.serialization's, as a consumer's build would
class PluginRegistrarConfigurator(testServices: TestServices) :
  EnvironmentConfigurator(testServices) {
  override fun ExtensionStorage.registerCompilerExtensions(
    module: TestModule,
    configuration: CompilerConfiguration,
  ) {
    FirExtensionRegistrarAdapter.registerExtension(FirSerializationExtensionRegistrar())
    with(FallbackCompilerPluginRegistrar()) { registerExtensions(configuration) }
    IrGenerationExtension.registerExtension(SerializationLoweringExtension())
  }
}

class TestLibrariesConfigurator(testServices: TestServices) :
  EnvironmentConfigurator(testServices) {
  override fun configureCompilerConfiguration(
    configuration: CompilerConfiguration,
    module: TestModule,
  ) {
    testLibraries.forEach(configuration::addJvmClasspathRoot)
  }
}

class TestLibrariesClasspathProvider(testServices: TestServices) :
  RuntimeClasspathProvider(testServices) {
  override fun runtimeClassPaths(module: TestModule): List<File> = testLibraries
}
