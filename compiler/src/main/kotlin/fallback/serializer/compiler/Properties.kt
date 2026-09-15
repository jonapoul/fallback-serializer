package fallback.serializer.compiler

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier

internal val MapOfCallableId = CallableId(FqName("kotlin.collections"), identifier("mapOf"))

internal val FallbackPredicate = DeclarationPredicate.create { annotated(FqNames.Fallback) }

internal object FallbackSerializerPluginKey : GeneratedDeclarationKey()

internal object FqNames {
  val FallbackSerializer = FqName("fallback.serializer")
  val Fallback = FallbackSerializer.child(identifier("Fallback"))

  val KotlinxSerialization = FqName("kotlinx.serialization")
  val SerialName = KotlinxSerialization.child(identifier("SerialName"))
}

internal object ClassIds {
  val FallbackEnumSerializer =
    ClassId(FqNames.FallbackSerializer, identifier("FallbackEnumSerializer"))

  val Serializable = ClassId(FqNames.KotlinxSerialization, identifier("Serializable"))
  val Pair = ClassId(FqName("kotlin"), identifier("Pair"))
}

internal object Names {
  val FallbackSerializer = identifier("FallbackSerializer")
  val Values = identifier("values")
  val With = identifier("with")
}
