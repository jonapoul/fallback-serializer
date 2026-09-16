package fallback.serializer.compiler

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar

internal class FallbackFirCheckersExtension(session: FirSession) :
  FirAdditionalCheckersExtension(session) {
  override val declarationCheckers: DeclarationCheckers =
    object : DeclarationCheckers() {
      override val classCheckers: Set<FirClassChecker> =
        setOf(
          FallbackSingleEntryChecker,
          FallbackSerializableChecker,
          FallbackSerializerNameChecker,
        )
    }

  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(FallbackPredicate)
  }
}
