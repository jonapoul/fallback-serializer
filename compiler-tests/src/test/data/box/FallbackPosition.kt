import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class FallbackFirst {
  @Fallback Unknown,
  Apple,
}

@Serializable
enum class FallbackOnly {
  @Fallback Unknown
}

fun box(): String {
  assertEquals(FallbackFirst.Apple, Json.decodeFromString<FallbackFirst>("\"Apple\""))
  assertEquals(FallbackFirst.Unknown, Json.decodeFromString<FallbackFirst>("\"Orange\""))
  assertEquals("\"Apple\"", Json.encodeToString(FallbackFirst.Apple))
  assertEquals(FallbackOnly.Unknown, Json.decodeFromString<FallbackOnly>("\"Unknown\""))
  assertEquals(FallbackOnly.Unknown, Json.decodeFromString<FallbackOnly>("\"Orange\""))
  return "OK"
}
