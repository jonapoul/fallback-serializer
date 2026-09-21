import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

// @Serializable without `with`: kotlinx.serialization's own companion serializer() should resolve to
// the generated nested `$serializer` rather than its default enum serializer
@Serializable
enum class Fruit {
  Apple,
  @Fallback Unknown,
}

@Serializable data class Order(val fruit: Fruit, val extras: List<Fruit>)

fun box(): String {
  assertEquals("\"Apple\"", Json.encodeToString(Fruit.Apple))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Mango\""))
  assertEquals(Fruit.Unknown, Json.decodeFromString(Fruit.serializer(), "\"Mango\""))

  val order = Json.decodeFromString<Order>("""{"fruit":"Orange","extras":["Apple","Mango"]}""")
  assertEquals(Order(Fruit.Unknown, listOf(Fruit.Apple, Fruit.Unknown)), order)
  return "OK"
}
