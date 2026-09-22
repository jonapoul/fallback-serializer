import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class Fruit {
  Apple,
  @Fallback Unknown,
}

@Serializable data class Order(val fruit: Fruit?)

fun box(): String {
  assertEquals(Order(Fruit.Unknown), Json.decodeFromString<Order>("""{"fruit":"Orange"}"""))
  assertEquals(Order(null), Json.decodeFromString<Order>("""{"fruit":null}"""))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit?>("\"Orange\""))
  assertEquals(null, Json.decodeFromString<Fruit?>("null"))
  return "OK"
}
