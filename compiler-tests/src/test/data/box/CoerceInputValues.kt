import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class OrderStatus {
  Pending,
  @Fallback Unknown,
}

@Serializable data class Order(val withDefault: OrderStatus = OrderStatus.Pending, val withoutDefault: OrderStatus)

fun box(): String {
  val json = Json { coerceInputValues = true }
  val order = json.decodeFromString<Order>("""{"withDefault":"Refunded","withoutDefault":"Refunded"}""")
  // Json swaps unknown values for the property's default before the serializer runs
  assertEquals(Order(withDefault = OrderStatus.Pending, withoutDefault = OrderStatus.Unknown), order)
  return "OK"
}
