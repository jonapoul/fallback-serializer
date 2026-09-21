package fallback.serializer.compiler

import fallback.serializer.compiler.FallbackErrors.MISSING_FALLBACK_SERIALIZER
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
// time. With a `with` argument, that serializer is used instead of the generated `$serializer` and
// the fallback is silently ignored.
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
    val isValid = annotation != null && annotation.getKClassArgument(Names.With) == null

    if (!isValid) {
      reporter.reportOn(
        source = declaration.source,
        factory = MISSING_FALLBACK_SERIALIZER,
        a = declaration.symbol.classId.shortClassName,
      )
    }
  }
}
