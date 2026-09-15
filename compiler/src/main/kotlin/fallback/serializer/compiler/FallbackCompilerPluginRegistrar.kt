package fallback.serializer.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

public class FallbackCompilerPluginRegistrar : CompilerPluginRegistrar() {
  override val pluginId: String = PLUGIN_ID
  override val supportsK2: Boolean = true

  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    FirExtensionRegistrarAdapter.registerExtension(FallbackFirExtensionRegistrar())
    IrGenerationExtension.registerExtension(FallbackIrGenerationExtension())
  }
}
