package fallback.serializer.compiler

import org.jetbrains.kotlin.descriptors.ClassKind.OBJECT
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.origin
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.plugin.createConstructor
import org.jetbrains.kotlin.fir.plugin.createNestedClass
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirEnumEntrySymbol
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

internal class FallbackFirDeclarationGenerationExtension(session: FirSession) :
  FirDeclarationGenerationExtension(session) {
  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(FallbackPredicate)
  }

  override fun getNestedClassifiersNames(
    classSymbol: FirClassSymbol<*>,
    context: NestedClassGenerationContext,
  ): Set<Name> =
    if (classSymbol.needsFallbackSerializer()) setOf(Names.FallbackSerializer) else emptySet()

  override fun generateNestedClassLikeDeclaration(
    owner: FirClassSymbol<*>,
    name: Name,
    context: NestedClassGenerationContext,
  ): FirClassLikeSymbol<*>? {
    if (name != Names.FallbackSerializer || !owner.needsFallbackSerializer()) return null
    return createNestedClass(owner, Names.FallbackSerializer, FallbackSerializerPluginKey, OBJECT) {
        superType(
          ClassIds.FallbackEnumSerializer.createConeType(session, arrayOf(owner.constructType()))
        )
      }
      .symbol
  }

  override fun getCallableNamesForClass(
    classSymbol: FirClassSymbol<*>,
    context: MemberGenerationContext,
  ): Set<Name> =
    if (classSymbol.origin == FallbackSerializerPluginKey.origin) {
      setOf(SpecialNames.INIT)
    } else {
      emptySet()
    }

  override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
    val owner = context.owner
    if (owner.origin != FallbackSerializerPluginKey.origin) return emptyList()
    val constructor =
      createConstructor(
        owner = owner,
        key = FallbackSerializerPluginKey,
        isPrimary = true,
        generateDelegatedNoArgConstructorCall = false,
      )
    return listOf(constructor.symbol)
  }

  // A FallbackSerializer declared by hand is reported by FallbackSerializerNameChecker instead
  @OptIn(DirectDeclarationsAccess::class)
  private fun FirClassSymbol<*>.needsFallbackSerializer(): Boolean =
    declarationSymbols.any {
      it is FirEnumEntrySymbol && session.predicateBasedProvider.matches(FallbackPredicate, it)
    } &&
      declarationSymbols.none { it is FirClassLikeSymbol<*> && it.name == Names.FallbackSerializer }
}
