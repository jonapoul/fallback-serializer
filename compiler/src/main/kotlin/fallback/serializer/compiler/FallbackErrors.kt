package fallback.serializer.compiler

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import org.jetbrains.kotlin.diagnostics.AbstractSourceElementPositioningStrategy
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory2
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.DECLARATION_NAME
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies.DEFAULT
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtElement

internal object FallbackErrors : KtDiagnosticsContainer() {
  val MULTIPLE_FALLBACK_ENTRIES by error<KtDiagnosticFactory2<Name, Name>>(KtElement::class)
  val MISSING_SERIALIZABLE by error<KtDiagnosticFactory1<Name>>(KtElement::class)
  val CUSTOM_SERIALIZER by error<KtDiagnosticFactory1<Name>>(KtElement::class)
  val MISSING_ACTUAL_FALLBACK_ENTRY by error<KtDiagnosticFactory2<Name, Name>>(KtElement::class)
  val DIFFERENT_ACTUAL_FALLBACK_ENTRY by error<KtDiagnosticFactory2<Name, Name>>(KtElement::class)
  val FALLBACK_OUTSIDE_ENUM_ENTRY by
    error<KtDiagnosticFactory0>(KtAnnotationEntry::class, strategy = DEFAULT)

  override fun getRendererFactory(): BaseDiagnosticRendererFactory = FallbackErrorMessages
}

// Replaces the compiler's error0/1/2 helpers. Kotlin 2.5.20 removed the PSI type parameter from the
// factory constructors, so pick whichever constructor this compiler has.
private inline fun <reified F : Any> KtDiagnosticsContainer.error(
  psiType: KClass<*>,
  strategy: AbstractSourceElementPositioningStrategy = DECLARATION_NAME,
) =
  PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, F>> { _, property ->
    val factory = newFactory(F::class.java, property.name, strategy, psiType, getRendererFactory())
    ReadOnlyProperty { _, _ -> factory }
  }

private fun <F : Any> newFactory(
  type: Class<F>,
  name: String,
  strategy: AbstractSourceElementPositioningStrategy,
  psiType: KClass<*>,
  rendererFactory: BaseDiagnosticRendererFactory,
): F {
  val constructor = type.constructors.single()
  val factory =
    if (constructor.parameterCount == 5) {
      constructor.newInstance(name, Severity.ERROR, strategy, psiType, rendererFactory)
    } else {
      constructor.newInstance(name, Severity.ERROR, strategy, rendererFactory)
    }
  return type.cast(factory)
}

// Messages use MessageFormat, so single quotes are doubled
private object FallbackErrorMessages : BaseDiagnosticRendererFactory() {
  override val MAP by
    KtDiagnosticFactoryToRendererMap("Fallback") { map ->
      map.put(
        FallbackErrors.MULTIPLE_FALLBACK_ENTRIES,
        "Enum class ''{0}'' can only have one @Fallback entry, ''{1}'' is already annotated.",
        CommonRenderers.NAME,
        CommonRenderers.NAME,
      )
      map.put(
        FallbackErrors.MISSING_SERIALIZABLE,
        "Enum class ''{0}'' has a @Fallback entry, so it must be annotated with @Serializable.",
        CommonRenderers.NAME,
      )
      map.put(
        FallbackErrors.CUSTOM_SERIALIZER,
        "Enum class ''{0}'' has a @Fallback entry, so @Serializable can''t have a ''with'' " +
          "argument. The generated serializer is applied automatically.",
        CommonRenderers.NAME,
      )
      map.put(
        FallbackErrors.MISSING_ACTUAL_FALLBACK_ENTRY,
        "Actual enum class ''{0}'' needs a @Fallback entry, since the expect enum marks ''{1}'' " +
          "with @Fallback.",
        CommonRenderers.NAME,
        CommonRenderers.NAME,
      )
      map.put(
        FallbackErrors.DIFFERENT_ACTUAL_FALLBACK_ENTRY,
        "@Fallback is on ''{0}'' here, but on ''{1}'' in the expect enum. Both must mark the same " +
          "entry.",
        CommonRenderers.NAME,
        CommonRenderers.NAME,
      )
      map.put(
        FallbackErrors.FALLBACK_OUTSIDE_ENUM_ENTRY,
        "@Fallback can only be used on enum entries.",
      )
    }
}
