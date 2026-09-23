import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class FallbackFirst {
  @Fallback Unknown,
  Sunny,
}

@Serializable
enum class FallbackOnly {
  @Fallback Unknown
}

fun box(): String {
  assertEquals(FallbackFirst.Sunny, Json.decodeFromString<FallbackFirst>("\"Sunny\""))
  assertEquals(FallbackFirst.Unknown, Json.decodeFromString<FallbackFirst>("\"Hail\""))
  assertEquals("\"Sunny\"", Json.encodeToString(FallbackFirst.Sunny))
  assertEquals(FallbackOnly.Unknown, Json.decodeFromString<FallbackOnly>("\"Unknown\""))
  assertEquals(FallbackOnly.Unknown, Json.decodeFromString<FallbackOnly>("\"Hail\""))
  return "OK"
}
