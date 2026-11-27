package fallback.serializer.compiler

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

public class FallbackCompilerPluginRegistrar : CompilerPluginRegistrar() {
  override val pluginId: String = PLUGIN_ID
  override val supportsK2: Boolean = true

  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    compat.registerExtensions(
      this,
      FallbackFirExtensionRegistrar(),
      FallbackIrGenerationExtension(),
    )
  }
}
