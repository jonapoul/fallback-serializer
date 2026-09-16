package fallback.serializer.compiler

import org.jetbrains.kotlin.test.backend.handlers.NoIrCompilationErrorsHandler
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureIrHandlersStep
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_DEXING
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.FULL_JDK
import org.jetbrains.kotlin.test.runners.codegen.AbstractFirLightTreeBlackBoxCodegenTest
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

// Compiles each file under src/test/data/box, then runs its box() function and expects "OK"
open class AbstractBoxTest : AbstractFirLightTreeBlackBoxCodegenTest() {
  // A getter, so subclasses can override it before configure() runs
  protected open val pluginOrder: PluginOrder
    get() = PluginOrder.SerializationFirst

  override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider =
    EnvironmentBasedStandardLibrariesPathProvider

  override fun configure(builder: TestConfigurationBuilder): Unit =
    with(builder) {
      super.configure(builder)

      configurePlugin(pluginOrder)

      defaultDirectives {
        +FULL_JDK
        +WITH_STDLIB
        +IGNORE_DEXING // Avoids loading R8 from the classpath
      }

      // Errors in the plugin's backend should fail the test without running box()
      configureIrHandlersStep { useHandlers(::NoIrCompilationErrorsHandler) }
    }
}
