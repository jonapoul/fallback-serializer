package fallback.serializer.compiler

import fallback.serializer.compiler.FallbackErrors.DIFFERENT_ACTUAL_FALLBACK_ENTRY
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
// one. Kotlin reports this too, but only as a missing `$serializer` member. Also reports one when
// the two mark different entries, since the actual one would silently win.
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
    val actualEntry =
      declaration.symbol.declarationSymbols.filterIsInstance<FirEnumEntrySymbol>().firstOrNull {
        context.session.predicateBasedProvider.matches(FallbackPredicate, it)
      }
    when {
      expectEntry == null -> Unit
      actualEntry == null ->
        reporter.reportOn(
          source = declaration.source,
          factory = MISSING_ACTUAL_FALLBACK_ENTRY,
          a = declaration.symbol.classId.shortClassName,
          b = expectEntry.name,
        )
      actualEntry.name != expectEntry.name ->
        reporter.reportOn(
          source = actualEntry.source,
          factory = DIFFERENT_ACTUAL_FALLBACK_ENTRY,
          a = actualEntry.name,
          b = expectEntry.name,
        )
    }
  }
}
