package fallback.serializer.compiler

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

internal class FallbackFirExtensionRegistrar : FirExtensionRegistrar() {
  override fun ExtensionRegistrarContext.configurePlugin() {
    +::FallbackFirDeclarationGenerationExtension
    +::FallbackFirCheckersExtension
    registerDiagnosticContainers(FallbackErrors)
  }
}
