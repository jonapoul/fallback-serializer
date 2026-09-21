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
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  // Entries with a @SerialName don't decode from their declared name
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Cherry\""))
  return "OK"
}
