package sample

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

// Enum shapes that the compiler tests only cover on the JVM
class ShapesTest {
  @Test
  fun `decodes enum with a companion object`() {
    assertEquals(expected = Veg.Unknown, actual = Json.decodeFromString("\"Orange\""))
    assertEquals(
      expected = Veg.Unknown,
      actual = Json.decodeFromString(Veg.serializer(), "\"Orange\""),
    )
  }

  @Test
  fun `decodes expect enum from common code`() =
    assertEquals(expected = Colour.Unknown, actual = Json.decodeFromString("\"Orange\""))

  @Test
  fun `decodes nested enum`() =
    assertEquals(expected = Crate.Size.Unknown, actual = Json.decodeFromString("\"Orange\""))

  @Test
  fun `decodes private enum`() =
    assertEquals(expected = "Unknown", actual = decodeSecret("\"Orange\""))

  @Test
  fun `descriptor has class annotations`() =
    assertEquals(
      expected = listOf("tagged"),
      actual = Tagged.serializer().descriptor.annotations.filterIsInstance<Tag>().map { it.value },
    )

  @Test
  fun `decodes map keys`() =
    assertEquals(
      expected = mapOf(OrderStatus.Pending to 1, OrderStatus.Unknown to 2),
      actual = Json.decodeFromString<Map<OrderStatus, Int>>("""{"Pending":1,"Refunded":2}"""),
    )
}
