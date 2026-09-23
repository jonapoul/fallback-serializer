import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class Weather {
  Sunny,
  Cloudy,
  @Fallback Unknown,
}

fun box(): String {
  // Unknown values all decode to the same entry, so later ones replace earlier ones
  assertEquals(
    mapOf(Weather.Sunny to 0, Weather.Unknown to 2),
    Json.decodeFromString<Map<Weather, Int>>("""{"Sunny":0,"Hail":1,"Snow":2}"""),
  )
  assertEquals(
    setOf(Weather.Sunny, Weather.Unknown),
    Json.decodeFromString<Set<Weather>>("""["Sunny","Hail","Snow"]"""),
  )
  return "OK"
}
