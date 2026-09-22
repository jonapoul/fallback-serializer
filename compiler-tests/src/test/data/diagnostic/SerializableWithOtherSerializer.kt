import fallback.serializer.Fallback
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FruitSerializer : KSerializer<Fruit> {
  override val descriptor = PrimitiveSerialDescriptor("Fruit", PrimitiveKind.STRING)
  override fun serialize(encoder: Encoder, value: Fruit) = encoder.encodeString(value.name)
  override fun deserialize(decoder: Decoder) = Fruit.valueOf(decoder.decodeString())
}

@Serializable(with = FruitSerializer::class)
<!CUSTOM_SERIALIZER!>enum class Fruit<!> {
  Apple,
  @Fallback Unknown,
}
