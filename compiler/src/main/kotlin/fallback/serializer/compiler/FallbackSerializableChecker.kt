package fallback.serializer.compiler

import fallback.serializer.compiler.FallbackErrors.CUSTOM_SERIALIZER
import fallback.serializer.compiler.FallbackErrors.MISSING_SERIALIZABLE
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind.Common
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getKClassArgument
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirEnumEntrySymbol

// Reports a compile error for enums with a @Fallback entry that aren't annotated with a plain
// @Serializable. Without the annotation, js, wasm and native can't resolve a serializer at compile
// time, and the JVM falls back to kotlinx.serialization's default enum serializer. With a `with`
// argument, that serializer is used instead of the generated `$serializer`. Either way the fallback
// would be silently ignored.
internal object FallbackSerializableChecker : FirClassChecker(Common) {
  @OptIn(DirectDeclarationsAccess::class)
  context(context: CheckerContext, reporter: DiagnosticReporter)
  override fun check(declaration: FirClass) {
    val hasFallback =
      declaration.symbol.declarationSymbols.filterIsInstance<FirEnumEntrySymbol>().any {
        context.session.predicateBasedProvider.matches(FallbackPredicate, it)
      }
    if (!hasFallback) return

    val annotation = declaration.getAnnotationByClassId(ClassIds.Serializable, context.session)
    val factory =
      when {
        annotation == null -> MISSING_SERIALIZABLE
        annotation.getKClassArgument(Names.With) != null -> CUSTOM_SERIALIZER
        else -> return
      }
    reporter.reportOn(
      source = declaration.source,
      factory = factory,
      a = declaration.symbol.classId.shortClassName,
    )
  }
}
