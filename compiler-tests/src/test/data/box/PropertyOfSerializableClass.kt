import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class Fruit {
  Apple,
  @Fallback Unknown,
}

@Serializable data class Order(val fruit: Fruit, val extras: List<Fruit>)

fun box(): String {
  val order = Json.decodeFromString<Order>("""{"fruit":"Orange","extras":["Apple","Mango"]}""")
  assertEquals(Order(Fruit.Unknown, listOf(Fruit.Apple, Fruit.Unknown)), order)
  return "OK"
}
