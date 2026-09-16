package fallback.serializer.compiler

import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.DECLARATION_NAME
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.error2
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers.NAME
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtElement

internal object FallbackErrors : KtDiagnosticsContainer() {
  val MULTIPLE_FALLBACK_ENTRIES by error2<KtElement, Name, Name>(DECLARATION_NAME)
  val MISSING_FALLBACK_SERIALIZER by error1<KtElement, Name>(DECLARATION_NAME)
  val DECLARED_FALLBACK_SERIALIZER by error1<KtElement, Name>(DECLARATION_NAME)
  val FALLBACK_OUTSIDE_ENUM_ENTRY by error0<KtAnnotationEntry>()

  override fun getRendererFactory(): BaseDiagnosticRendererFactory = FallbackErrorMessages
}

// Messages use MessageFormat, so single quotes are doubled
private object FallbackErrorMessages : BaseDiagnosticRendererFactory() {
  override val MAP by
    KtDiagnosticFactoryToRendererMap("Fallback") { map ->
      map.put(
        FallbackErrors.MULTIPLE_FALLBACK_ENTRIES,
        "Enum class ''{0}'' can only have one @Fallback entry, ''{1}'' is already annotated.",
        NAME,
        NAME,
      )
      map.put(
        FallbackErrors.MISSING_FALLBACK_SERIALIZER,
        "Enum class ''{0}'' has a @Fallback entry, so it must be annotated with " +
          "@Serializable(with = {0}.FallbackSerializer::class).",
        NAME,
      )
      map.put(
        FallbackErrors.DECLARED_FALLBACK_SERIALIZER,
        "Enum class ''{0}'' has a @Fallback entry, so ''FallbackSerializer'' is generated and " +
          "can''t be declared.",
        NAME,
      )
      map.put(
        FallbackErrors.FALLBACK_OUTSIDE_ENUM_ENTRY,
        "@Fallback can only be used on enum entries.",
      )
    }
}
