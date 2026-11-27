package fallback.serializer.compiler.compat

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

// Compiler APIs that differ between the supported Kotlin versions. Each module next to this one
// implements it against the oldest Kotlin version it covers.
interface CompatContext {
  fun registerExtensions(
    storage: ExtensionStorage,
    fir: FirExtensionRegistrar,
    ir: IrGenerationExtension,
  )

  fun findClass(context: IrPluginContext, classId: ClassId): IrClassSymbol?

  fun findFunctions(
    context: IrPluginContext,
    callableId: CallableId,
  ): Collection<IrSimpleFunctionSymbol>

  // Kotlin 2.4.0 changed the element type to IrAnnotation, a subclass of IrConstructorCall
  fun annotations(container: IrAnnotationContainer): List<IrConstructorCall>

  // Kotlin 2.4.0 dropped the session parameter
  fun getKClassArgument(annotation: FirAnnotation, name: Name, session: FirSession): ConeKotlinType?
}
