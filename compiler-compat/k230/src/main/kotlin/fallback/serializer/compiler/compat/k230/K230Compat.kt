package fallback.serializer.compiler.compat.k230

import fallback.serializer.compiler.compat.CompatContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getKClassArgument
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

// Kotlin 2.3.x
object K230Compat : CompatContext {
  override fun registerExtensions(
    storage: ExtensionStorage,
    fir: FirExtensionRegistrar,
    ir: IrGenerationExtension,
  ) {
    with(storage) {
      FirExtensionRegistrarAdapter.registerExtension(fir)
      IrGenerationExtension.registerExtension(ir)
    }
  }

  override fun findClass(context: IrPluginContext, classId: ClassId): IrClassSymbol? =
    context.referenceClass(classId)

  override fun findFunctions(
    context: IrPluginContext,
    callableId: CallableId,
  ): Collection<IrSimpleFunctionSymbol> = context.referenceFunctions(callableId)

  override fun annotations(container: IrAnnotationContainer): List<IrConstructorCall> =
    container.annotations

  override fun getKClassArgument(
    annotation: FirAnnotation,
    name: Name,
    session: FirSession,
  ): ConeKotlinType? = annotation.getKClassArgument(name, session)
}
