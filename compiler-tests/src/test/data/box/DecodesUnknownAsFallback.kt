import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class OrderStatus {
  Pending,
  @SerialName("in_transit") Shipped,
  Delivered,
  @Fallback Unknown,
}

fun box(): String {
  assertEquals(OrderStatus.Unknown, Json.decodeFromString<OrderStatus>("\"Refunded\""))
  assertEquals(OrderStatus.Unknown, Json.decodeFromString(OrderStatus.serializer(), "\"Refunded\""))
  // Entries with a @SerialName don't decode from their declared name
  assertEquals(OrderStatus.Unknown, Json.decodeFromString<OrderStatus>("\"Shipped\""))
  return "OK"
}
