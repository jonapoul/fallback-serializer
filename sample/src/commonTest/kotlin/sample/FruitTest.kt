package sample

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

// Runs on every target, to check the generated serializer works on each backend
class FruitTest {
  @Test fun `decodes by name`() = assertEquals(expected = Fruit.Apple, actual = decode("Apple"))

  @Test
  fun `decodes by serial name`() =
    assertEquals(expected = Fruit.Cherry, actual = decode("cherry_pie"))

  @Test
  fun `decodes by alternative name`() =
    assertEquals(expected = Fruit.Banana, actual = decode("banana"))

  @Test
  fun `decodes unknown value as fallback`() =
    assertEquals(expected = Fruit.Unknown, actual = decode("Orange"))

  @Test
  fun `encodes by serial name`() =
    assertEquals(expected = "\"cherry_pie\"", actual = Json.encodeToString(Fruit.Cherry))

  @Test
  fun `uses class serial name`() =
    assertEquals(expected = "fruit", actual = Fruit.FallbackSerializer.descriptor.serialName)

  @Test
  fun `decodes inside a class`() =
    assertEquals(
      expected = Basket(listOf(Fruit.Apple, Fruit.Unknown)),
      actual = Json.decodeFromString<Basket>("""{"fruit":["Apple","Orange"]}"""),
    )

  private fun decode(value: String): Fruit = Json.decodeFromString("\"$value\"")
}
