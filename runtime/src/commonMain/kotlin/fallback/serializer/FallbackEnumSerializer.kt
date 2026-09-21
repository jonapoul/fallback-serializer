package fallback.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind.ENUM
import kotlinx.serialization.descriptors.StructureKind.OBJECT
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for enums with a fallback value. Also the base class of the `$serializer` object
 * generated inside each enum with a [Fallback] entry.
 *
 * [valueAnnotations] and [annotations] are the `@SerialInfo` annotations of the entries and the
 * enum class, such as `@JsonNames`. They're added to the [descriptor].
 */
public open class FallbackEnumSerializer<E : Enum<E>>(
  serialName: String,
  private val values: Array<E>,
  private val valueSerialNames: Map<E, String>,
  private val fallbackValue: E,
  valueAnnotations: Map<E, List<Annotation>> = emptyMap(),
  annotations: List<Annotation> = emptyList(),
) : KSerializer<E> {
  @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
  override val descriptor: SerialDescriptor =
    buildSerialDescriptor(serialName = serialName, kind = ENUM) {
      this.annotations = annotations
      for (value in values) {
        val valueSerialName = valueSerialNames[value] ?: value.name
        element(
          elementName = valueSerialName,
          descriptor =
            buildSerialDescriptor(serialName = "$serialName.$valueSerialName", kind = OBJECT),
          annotations = valueAnnotations[value].orEmpty(),
        )
      }
    }

  override fun serialize(encoder: Encoder, value: E) {
    val index = values.indexOf(value)
    check(index != -1) {
      "$value is not a valid enum ${descriptor.serialName}, must be one of ${values.contentToString()}"
    }
    encoder.encodeEnum(descriptor, index)
  }

  override fun deserialize(decoder: Decoder): E {
    val index =
      try {
        decoder.decodeEnum(descriptor)
      } catch (_: SerializationException) {
        -1
      }
    return if (index >= 0) values[index] else fallbackValue
  }

  override fun toString(): String = "FallbackEnumSerializer<${descriptor.serialName}>"
}

public inline fun <reified E : Enum<E>> FallbackEnumSerializer(
  serialName: String,
  fallbackValue: E,
  valueSerialNames: Map<E, String> = emptyMap(),
  valueAnnotations: Map<E, List<Annotation>> = emptyMap(),
  annotations: List<Annotation> = emptyList(),
): FallbackEnumSerializer<E> =
  FallbackEnumSerializer(
    serialName = serialName,
    values = enumValues<E>(),
    valueSerialNames = valueSerialNames,
    fallbackValue = fallbackValue,
    valueAnnotations = valueAnnotations,
    annotations = annotations,
  )
