package fallback.serializer.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

public class FallbackCommandLineProcessor : CommandLineProcessor {
  override val pluginId: String = PLUGIN_ID
  override val pluginOptions: Collection<AbstractCliOption> = emptyList()

  override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration,
  ) {
    // No options are declared, so this is never called
  }
}
