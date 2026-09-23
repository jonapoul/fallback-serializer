import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class Currency {
  @SerialName("GBP") PoundSterling,
  @Fallback Unknown,
}

@Serializable data class Price(val currency: Currency?)

fun box(): String {
  assertEquals(Price(Currency.Unknown), Json.decodeFromString<Price>("""{"currency":"JPY"}"""))
  assertEquals(Price(null), Json.decodeFromString<Price>("""{"currency":null}"""))
  assertEquals(Currency.Unknown, Json.decodeFromString<Currency?>("\"JPY\""))
  assertEquals(null, Json.decodeFromString<Currency?>("null"))
  return "OK"
}
