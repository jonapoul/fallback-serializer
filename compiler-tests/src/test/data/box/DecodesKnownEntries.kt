import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable(with = Fruit.FallbackSerializer::class)
enum class Fruit {
  Apple,
  @SerialName("cherry_pie") Cherry,
  @Fallback Unknown,
}

fun box(): String {
  assertEquals(Fruit.Apple, Json.decodeFromString<Fruit>("\"Apple\""))
  assertEquals(Fruit.Cherry, Json.decodeFromString<Fruit>("\"cherry_pie\""))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Unknown\""))
  return "OK"
}
