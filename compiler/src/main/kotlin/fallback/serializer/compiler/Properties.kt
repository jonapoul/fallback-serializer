package fallback.serializer.compiler

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier

internal val ListOfCallableId = CallableId(FqName("kotlin.collections"), identifier("listOf"))
internal val MapOfCallableId = CallableId(FqName("kotlin.collections"), identifier("mapOf"))

internal val FallbackPredicate = DeclarationPredicate.create { annotated(FqNames.Fallback) }

internal object FallbackSerializerPluginKey : GeneratedDeclarationKey()

internal object FqNames {
  val FallbackSerializer = FqName("fallback.serializer")
  val Fallback = FallbackSerializer.child(identifier("Fallback"))

  val KotlinxSerialization = FqName("kotlinx.serialization")
  val SerialName = KotlinxSerialization.child(identifier("SerialName"))

  // Annotation classes marked with any of these are kept in descriptors, same as
  // kotlinx.serialization
  val SerialInfoMarkers =
    listOf(
      KotlinxSerialization.child(identifier("SerialInfo")),
      KotlinxSerialization.child(identifier("InheritableSerialInfo")),
      KotlinxSerialization.child(identifier("MetaSerializable")),
    )
}

internal object ClassIds {
  val Fallback = ClassId(FqNames.FallbackSerializer, identifier("Fallback"))
  val FallbackEnumSerializer =
    ClassId(FqNames.FallbackSerializer, identifier("FallbackEnumSerializer"))

  val Serializable = ClassId(FqNames.KotlinxSerialization, identifier("Serializable"))
  val Pair = ClassId(FqName("kotlin"), identifier("Pair"))
}

internal object Names {
  // The name kotlinx.serialization looks for when resolving an enum's serializer, before it falls
  // back to its own EnumSerializer. Generating it means no `@Serializable(with = ...)` is needed.
  val GeneratedSerializer = identifier("\$serializer")

  val Values = identifier("values")
  val With = identifier("with")
}
