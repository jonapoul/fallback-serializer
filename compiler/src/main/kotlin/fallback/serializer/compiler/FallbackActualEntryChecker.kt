package fallback.serializer.compiler

import fallback.serializer.compiler.FallbackErrors.MISSING_ACTUAL_FALLBACK_ENTRY
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind.Platform
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.getSingleMatchedExpectForActualOrNull
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirEnumEntrySymbol

// Reports a compile error for an actual enum without a @Fallback entry when its expect enum has
// one. Kotlin reports this too, but only as a missing `$serializer` member.
internal object FallbackActualEntryChecker : FirClassChecker(Platform) {
  @OptIn(DirectDeclarationsAccess::class)
  context(context: CheckerContext, reporter: DiagnosticReporter)
  override fun check(declaration: FirClass) {
    if (!declaration.status.isActual) return
    val expect = declaration.symbol.getSingleMatchedExpectForActualOrNull() as? FirClassSymbol<*>
    val expectEntry =
      expect?.declarationSymbols?.filterIsInstance<FirEnumEntrySymbol>()?.firstOrNull {
        it.hasAnnotation(ClassIds.Fallback, context.session)
      }
    val hasActualEntry =
      declaration.symbol.declarationSymbols.filterIsInstance<FirEnumEntrySymbol>().any {
        context.session.predicateBasedProvider.matches(FallbackPredicate, it)
      }
    if (expectEntry == null || hasActualEntry) return

    reporter.reportOn(
      source = declaration.source,
      factory = MISSING_ACTUAL_FALLBACK_ENTRY,
      a = declaration.symbol.classId.shortClassName,
      b = expectEntry.name,
    )
  }
}
