package fallback.serializer.compiler

import fallback.serializer.compiler.FallbackErrors.FALLBACK_OUTSIDE_ENUM_ENTRY
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind.Common
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId

// Reports a compile error for @Fallback on a property. Its target allows properties because that's
// how Kotlin sees enum entries, but it only has an effect on enum entries.
internal object FallbackTargetChecker : FirPropertyChecker(Common) {
  context(context: CheckerContext, reporter: DiagnosticReporter)
  override fun check(declaration: FirProperty) {
    val annotation =
      declaration.getAnnotationByClassId(ClassIds.Fallback, context.session) ?: return
    reporter.reportOn(source = annotation.source, factory = FALLBACK_OUTSIDE_ENUM_ENTRY)
  }
}
