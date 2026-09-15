package fallback.serializer.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin.GeneratedByPlugin
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrMutableAnnotationContainer
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.deepCopyWithoutPatchingParents
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
      val builtIns = pluginContext.irBuiltIns
      val serializerClass = constructor.parentAsClass
      val enumClass = serializerClass.parentAsClass
      val enumType = enumClass.defaultType
      val annotationListType = builtIns.listClass.typeWith(builtIns.annotationType)

      val entries = enumClass.declarations.filterIsInstance<IrEnumEntry>()
      val fallback = entries.first { it.hasAnnotation(FqNames.Fallback) }

      // Only entries with a @SerialName, the rest fall back to their name at runtime
      val valueSerialNames = entries.mapNotNull { entry ->
        val serialName =
          entry.getAnnotationArgumentValue<String>(FqNames.SerialName, "value")
            ?: return@mapNotNull null
        irGetEnumValue(enumType, entry) to irString(serialName)
      }

      // Only entries with annotations to keep, such as @JsonNames
      val valueAnnotations = entries.mapNotNull { entry ->
        val annotations =
          entry.serialInfoAnnotations().ifEmpty {
            return@mapNotNull null
          }
        irGetEnumValue(enumType, entry) to
          irListOf(pluginContext, builtIns.annotationType, annotations)
      }

      val valuesFunction =
        enumClass.functions.single { it.name == Names.Values && it.parameters.isEmpty() }

      val serializerBaseClass =
        checkNotNull(pluginContext.finderForBuiltins().findClass(ClassIds.FallbackEnumSerializer))
      val baseConstructor = checkNotNull(serializerBaseClass.owner.primaryConstructor)

      +irDelegatingConstructorCall(baseConstructor).apply {
        typeArguments[0] = enumType
        arguments[0] =
          irString(
            enumClass.getAnnotationArgumentValue<String>(FqNames.SerialName, "value")
              ?: enumClass.kotlinFqName.asString()
          )
        arguments[1] = irCall(valuesFunction)
        arguments[2] =
          irMapOf(
            pluginContext = pluginContext,
            keyType = enumType,
            valueType = builtIns.stringType,
            entries = valueSerialNames,
          )
        arguments[3] = irGetEnumValue(enumType, fallback)
        arguments[4] =
          irMapOf(
            pluginContext = pluginContext,
            keyType = enumType,
            valueType = annotationListType,
            entries = valueAnnotations,
          )
        arguments[5] =
          irListOf(pluginContext, builtIns.annotationType, enumClass.serialInfoAnnotations())
      }
      +IrInstanceInitializerCallImpl(
        startOffset = startOffset,
        endOffset = endOffset,
        classSymbol = serializerClass.symbol,
        type = builtIns.unitType,
      )
    }

  private fun IrBuilderWithScope.irGetEnumValue(type: IrType, entry: IrEnumEntry): IrExpression =
    IrGetEnumValueImpl(startOffset, endOffset, type, entry.symbol)

  @OptIn(UnsafeDuringIrConstructionAPI::class)
  private fun IrBuilderWithScope.irMapOf(
    pluginContext: IrPluginContext,
    keyType: IrType,
    valueType: IrType,
    entries: List<Pair<IrExpression, IrExpression>>,
  ): IrExpression {
    val finder = pluginContext.finderForBuiltins()
    val pairClass = checkNotNull(finder.findClass(ClassIds.Pair)).owner
    val pairConstructor = checkNotNull(pairClass.primaryConstructor).symbol
    val pairType = pairClass.typeWith(keyType, valueType)
    val pairs = entries.map { (key, value) ->
      irCall(pairConstructor, pairType, pairClass).apply {
        typeArguments[0] = keyType
        typeArguments[1] = valueType
        arguments[0] = key
        arguments[1] = value
      }
    }

    val mapOf =
      finder.findFunctions(MapOfCallableId).single {
        it.owner.parameters.singleOrNull()?.varargElementType != null
      }
    return irCall(mapOf, pluginContext.irBuiltIns.mapClass.typeWith(keyType, valueType)).apply {
      typeArguments[0] = keyType
      typeArguments[1] = valueType
      arguments[0] = irVararg(pairType, pairs)
    }
  }

  @OptIn(UnsafeDuringIrConstructionAPI::class)
  private fun IrBuilderWithScope.irListOf(
    pluginContext: IrPluginContext,
    elementType: IrType,
    elements: List<IrExpression>,
  ): IrExpression {
    val listOf =
      pluginContext.finderForBuiltins().findFunctions(ListOfCallableId).single {
        it.owner.parameters.singleOrNull()?.varargElementType != null
      }
    return irCall(listOf, pluginContext.irBuiltIns.listClass.typeWith(elementType)).apply {
      typeArguments[0] = elementType
      arguments[0] = irVararg(elementType, elements)
    }
  }

  // Copies of the annotations that kotlinx.serialization would keep in the descriptor
  @OptIn(UnsafeDuringIrConstructionAPI::class)
  private fun IrMutableAnnotationContainer.serialInfoAnnotations(): List<IrExpression> =
    annotations
      .filter { annotation ->
        FqNames.SerialInfoMarkers.any { annotation.classSymbol.owner.hasAnnotation(it) }
      }
      .map { it.deepCopyWithoutPatchingParents() }
}
