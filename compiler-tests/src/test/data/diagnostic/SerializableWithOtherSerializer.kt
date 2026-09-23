import fallback.serializer.Fallback
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object CurrencySerializer : KSerializer<Currency> {
  override val descriptor = PrimitiveSerialDescriptor("Currency", PrimitiveKind.STRING)
  override fun serialize(encoder: Encoder, value: Currency) = encoder.encodeString(value.name)
  override fun deserialize(decoder: Decoder) = Currency.valueOf(decoder.decodeString())
}

@Serializable(with = CurrencySerializer::class)
<!CUSTOM_SERIALIZER!>enum class Currency<!> {
  @SerialName("GBP") PoundSterling,
  @Fallback Unknown,
}
