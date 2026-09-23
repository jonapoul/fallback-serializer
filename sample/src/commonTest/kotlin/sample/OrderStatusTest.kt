package sample

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

// Runs on every target, to check the generated serializer works on each backend
class OrderStatusTest {
  @Test
  fun `decodes by name`() = assertEquals(expected = OrderStatus.Pending, actual = decode("Pending"))

  @Test
  fun `decodes by serial name`() =
    assertEquals(expected = OrderStatus.Shipped, actual = decode("in_transit"))

  @Test
  fun `decodes by alternative name`() =
    assertEquals(expected = OrderStatus.Delivered, actual = decode("complete"))

  @Test
  fun `decodes unknown value as fallback`() =
    assertEquals(expected = OrderStatus.Unknown, actual = decode("Refunded"))

  @Test
  fun `encodes by serial name`() =
    assertEquals(expected = "\"in_transit\"", actual = Json.encodeToString(OrderStatus.Shipped))

  @Test
  fun `uses class serial name`() =
    assertEquals(expected = "order_status", actual = OrderStatus.serializer().descriptor.serialName)

  @Test
  fun `decodes inside a class`() =
    assertEquals(
      expected = Order(listOf(OrderStatus.Pending, OrderStatus.Unknown)),
      actual = Json.decodeFromString<Order>("""{"history":["Pending","Refunded"]}"""),
    )

  private fun decode(value: String): OrderStatus = Json.decodeFromString("\"$value\"")
}
