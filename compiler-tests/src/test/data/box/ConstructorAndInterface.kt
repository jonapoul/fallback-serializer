import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

interface Coded {
  val code: Int
}

@Serializable
enum class Fruit(override val code: Int) : Coded {
  Apple(1),
  @Fallback Unknown(0),
}

fun box(): String {
  assertEquals(Fruit.Apple, Json.decodeFromString<Fruit>("\"Apple\""))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  assertEquals(0, Json.decodeFromString<Fruit>("\"Orange\"").code)
  return "OK"
}
