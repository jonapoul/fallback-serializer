package fallback.serializer.compiler

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind.Common
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors.OTHER_ERROR_WITH_REASON
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getKClassArgument
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirEnumEntrySymbol
import org.jetbrains.kotlin.fir.types.classId

// Reports a compile error for enums with a @Fallback entry that aren't annotated with
// @Serializable(with = <Enum>.FallbackSerializer::class), otherwise kotlinx.serialization uses its
// default enum serializer and the fallback is silently ignored
internal object FallbackSerializableChecker : FirClassChecker(Common) {
  @OptIn(DirectDeclarationsAccess::class)
  context(context: CheckerContext, reporter: DiagnosticReporter)
  override fun check(declaration: FirClass) {
    val hasFallback =
      declaration.symbol.declarationSymbols.filterIsInstance<FirEnumEntrySymbol>().any {
        context.session.predicateBasedProvider.matches(FallbackPredicate, it)
      }
    if (!hasFallback) return

    val classId = declaration.symbol.classId
    val serializerType =
      declaration
        .getAnnotationByClassId(ClassIds.Serializable, context.session)
        ?.getKClassArgument(Names.With)
    if (serializerType?.classId == classId.createNestedClassId(Names.FallbackSerializer)) return

    val name = classId.shortClassName
    reporter.reportOn(
      source = declaration.source,
      factory = OTHER_ERROR_WITH_REASON,
      a =
        "enum class '$name' has a @Fallback entry, so it must be annotated with @Serializable(with = " +
          "$name.FallbackSerializer::class)",
    )
  }
}
