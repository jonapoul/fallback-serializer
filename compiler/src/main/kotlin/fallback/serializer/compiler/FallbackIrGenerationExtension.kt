package fallback.serializer.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin.GeneratedByPlugin
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getAnnotationArgumentValue
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

internal class FallbackIrGenerationExtension : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    moduleFragment.transformChildrenVoid(
      transformer =
        object : IrElementTransformerVoid() {
          override fun visitConstructor(declaration: IrConstructor): IrStatement {
            if (declaration.origin == GeneratedByPlugin(FallbackSerializerPluginKey)) {
              declaration.body = buildBody(pluginContext, declaration)
            }
            return super.visitConstructor(declaration)
          }
        }
    )
  }

  // Argument indices of the FallbackEnumSerializer constructor
  @Suppress("MagicNumber")
  @OptIn(UnsafeDuringIrConstructionAPI::class)
  private fun buildBody(pluginContext: IrPluginContext, constructor: IrConstructor) =
    DeclarationIrBuilder(pluginContext, constructor.symbol).irBlockBody {
      val serializerClass = constructor.parentAsClass
      val enumClass = serializerClass.parentAsClass
      val enumType = enumClass.defaultType
      val stringType = pluginContext.irBuiltIns.stringType

      val entries = enumClass.declarations.filterIsInstance<IrEnumEntry>()
      val fallback = entries.first { it.hasAnnotation(FqNames.Fallback) }

      val pairClass = checkNotNull(pluginContext.finderForBuiltins().findClass(ClassIds.Pair)).owner
      val pairConstructor = checkNotNull(pairClass.primaryConstructor).symbol
      val pairType = pairClass.typeWith(enumType, stringType)

      // Only entries with a @SerialName, the rest fall back to their name at runtime
      val serialNamePairs = entries.mapNotNull { entry ->
        val serialName =
          entry.getAnnotationArgumentValue<String>(FqNames.SerialName, "value")
            ?: return@mapNotNull null
        irCall(pairConstructor, pairType, pairClass).apply {
          typeArguments[0] = enumType
          typeArguments[1] = stringType
          arguments[0] = IrGetEnumValueImpl(startOffset, endOffset, enumType, entry.symbol)
          arguments[1] = irString(serialName)
        }
      }

      val mapOf =
        pluginContext.finderForBuiltins().findFunctions(MapOfCallableId).single {
          it.owner.parameters.singleOrNull()?.varargElementType != null
        }
      val valueSerialNames =
        irCall(mapOf, pluginContext.irBuiltIns.mapClass.typeWith(enumType, stringType)).apply {
          typeArguments[0] = enumType
          typeArguments[1] = stringType
          arguments[0] = irVararg(pairType, serialNamePairs)
        }

      val valuesFunction =
        enumClass.functions.single { it.name == Names.Values && it.parameters.isEmpty() }

      val serializerBaseClass =
        checkNotNull(pluginContext.finderForBuiltins().findClass(ClassIds.FallbackEnumSerializer))
      val baseConstructor = checkNotNull(serializerBaseClass.owner.primaryConstructor)

      +irDelegatingConstructorCall(baseConstructor).apply {
        typeArguments[0] = enumType
        arguments[0] = irString(enumClass.kotlinFqName.asString())
        arguments[1] = irCall(valuesFunction)
        arguments[2] = valueSerialNames
        arguments[3] = IrGetEnumValueImpl(startOffset, endOffset, enumType, fallback.symbol)
      }
      +IrInstanceInitializerCallImpl(
        startOffset = startOffset,
        endOffset = endOffset,
        classSymbol = serializerClass.symbol,
        type = pluginContext.irBuiltIns.unitType,
      )
    }
}
