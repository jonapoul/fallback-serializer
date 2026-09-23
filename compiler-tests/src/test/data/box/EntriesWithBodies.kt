import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class PaymentMethod {
  Card {
    override val code = "CARD"
  },
  @Fallback
  Unsupported {
    override val code = "?"
  };

  abstract val code: String
}

fun box(): String {
  assertEquals(PaymentMethod.Card, Json.decodeFromString<PaymentMethod>("\"Card\""))
  assertEquals(PaymentMethod.Unsupported, Json.decodeFromString<PaymentMethod>("\"Crypto\""))
  assertEquals("?", Json.decodeFromString<PaymentMethod>("\"Crypto\"").code)
  assertEquals("\"Unsupported\"", Json.encodeToString(PaymentMethod.Unsupported))
  return "OK"
}
