import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class Fruit {
  Apple {
    override val colour = "red"
  },
  @Fallback
  Unknown {
    override val colour = "none"
  };

  abstract val colour: String
}

fun box(): String {
  assertEquals(Fruit.Apple, Json.decodeFromString<Fruit>("\"Apple\""))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  assertEquals("\"Unknown\"", Json.encodeToString(Fruit.Unknown))
  return "OK"
}
