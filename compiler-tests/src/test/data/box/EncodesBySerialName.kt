import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class Currency {
  @SerialName("GBP") PoundSterling,
  @SerialName("EUR") Euro,
  Dollar,
  @Fallback Unknown,
}

fun box(): String {
  assertEquals("\"GBP\"", Json.encodeToString<Currency>(Currency.PoundSterling))
  assertEquals("\"EUR\"", Json.encodeToString<Currency>(Currency.Euro))
  assertEquals("\"Dollar\"", Json.encodeToString<Currency>(Currency.Dollar))
  assertEquals("\"Unknown\"", Json.encodeToString<Currency>(Currency.Unknown))
  return "OK"
}
