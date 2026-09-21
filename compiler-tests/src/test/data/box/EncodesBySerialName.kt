import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class Fruit {
  Apple,
  @SerialName("cherry_pie") Cherry,
  @Fallback Unknown,
}

fun box(): String {
  assertEquals("\"Apple\"", Json.encodeToString<Fruit>(Fruit.Apple))
  assertEquals("\"cherry_pie\"", Json.encodeToString<Fruit>(Fruit.Cherry))
  assertEquals("\"Unknown\"", Json.encodeToString<Fruit>(Fruit.Unknown))
  return "OK"
}
