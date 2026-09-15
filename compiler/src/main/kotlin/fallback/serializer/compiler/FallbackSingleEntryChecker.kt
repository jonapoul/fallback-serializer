package fallback.serializer.compiler

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind.Common
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.OTHER_ERROR_WITH_REASON
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirEnumEntrySymbol

// Reports a compile error for enums with more than one @Fallback entry
internal object FallbackSingleEntryChecker : FirClassChecker(Common) {
  @OptIn(DirectDeclarationsAccess::class)
  context(context: CheckerContext, reporter: DiagnosticReporter)
  override fun check(declaration: FirClass) {
    val entries =
      declaration.symbol.declarationSymbols.filterIsInstance<FirEnumEntrySymbol>().filter {
        context.session.predicateBasedProvider.matches(FallbackPredicate, it)
      }
    if (entries.size <= 1) return

    val first = entries.first().name
    val name = declaration.symbol.classId.shortClassName
    for (entry in entries.drop(1)) {
      reporter.reportOn(
        source = entry.source,
        factory = OTHER_ERROR_WITH_REASON,
        a = "enum class '$name' can only have one @Fallback entry, '$first' is already annotated",
      )
    }
  }
}
