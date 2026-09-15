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

// The order the compiler runs the two plugins in, for both FIR and IR. A real build can set it with
// -Xcompiler-plugin-order.
enum class PluginOrder {
  SerializationFirst,
  FallbackFirst,
}

fun TestConfigurationBuilder.configurePlugin(order: PluginOrder = PluginOrder.SerializationFirst) {
  useConfigurators({ PluginRegistrarConfigurator(it, order) }, ::TestLibrariesConfigurator)
  useCustomRuntimeClasspathProviders(::TestLibrariesClasspathProvider)
}

// Registers our plugin alongside kotlinx.serialization's, as a consumer's build would
class PluginRegistrarConfigurator(testServices: TestServices, private val order: PluginOrder) :
  EnvironmentConfigurator(testServices) {
  override fun ExtensionStorage.registerCompilerExtensions(
    module: TestModule,
    configuration: CompilerConfiguration,
  ) {
    if (order == PluginOrder.FallbackFirst) registerFallback(configuration)
    FirExtensionRegistrarAdapter.registerExtension(FirSerializationExtensionRegistrar())
    IrGenerationExtension.registerExtension(SerializationLoweringExtension())
    if (order == PluginOrder.SerializationFirst) registerFallback(configuration)
  }

  private fun ExtensionStorage.registerFallback(configuration: CompilerConfiguration) =
    with(FallbackCompilerPluginRegistrar()) { registerExtensions(configuration) }
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
