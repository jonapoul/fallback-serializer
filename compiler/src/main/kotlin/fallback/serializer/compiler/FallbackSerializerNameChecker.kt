package fallback.serializer.compiler

import fallback.serializer.compiler.FallbackErrors.DECLARED_FALLBACK_SERIALIZER
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind.Common
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirEnumEntrySymbol

// Reports a compile error for a FallbackSerializer declared inside an enum with a @Fallback entry,
// since the plugin generates one there. Generation is skipped when this is reported.
internal object FallbackSerializerNameChecker : FirClassChecker(Common) {
  @OptIn(DirectDeclarationsAccess::class)
  context(context: CheckerContext, reporter: DiagnosticReporter)
  override fun check(declaration: FirClass) {
    val declarations = declaration.symbol.declarationSymbols
    val hasFallback =
      declarations.filterIsInstance<FirEnumEntrySymbol>().any {
        context.session.predicateBasedProvider.matches(FallbackPredicate, it)
      }
    if (!hasFallback) return

    val name = declaration.symbol.classId.shortClassName
    for (nested in declarations.filterIsInstance<FirClassLikeSymbol<*>>()) {
      if (nested.name != Names.FallbackSerializer) continue
      reporter.reportOn(source = nested.source, factory = DECLARED_FALLBACK_SERIALIZER, a = name)
    }
  }
}
