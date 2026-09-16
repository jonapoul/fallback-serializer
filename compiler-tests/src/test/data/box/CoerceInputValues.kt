import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable(with = Fruit.FallbackSerializer::class)
enum class Fruit {
  Apple,
  @Fallback Unknown,
}

@Serializable data class Order(val withDefault: Fruit = Fruit.Apple, val withoutDefault: Fruit)

fun box(): String {
  val json = Json { coerceInputValues = true }
  val order = json.decodeFromString<Order>("""{"withDefault":"Orange","withoutDefault":"Orange"}""")
  // Json swaps unknown values for the property's default before the serializer runs
  assertEquals(Order(withDefault = Fruit.Apple, withoutDefault = Fruit.Unknown), order)
  return "OK"
}
