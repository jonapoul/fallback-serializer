import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@SerialName("currency")
@Serializable
enum class Currency {
  @SerialName("GBP") PoundSterling,
  @Fallback Unknown,
}

fun box(): String {
  assertEquals("currency", Currency.serializer().descriptor.serialName)
  assertEquals("currency.GBP", Currency.serializer().descriptor.getElementDescriptor(0).serialName)
  assertEquals(Currency.Unknown, Json.decodeFromString<Currency>("\"JPY\""))
  return "OK"
}
