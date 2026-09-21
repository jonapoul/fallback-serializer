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
    assertEquals(expected = "fruit", actual = Fruit.serializer().descriptor.serialName)

  @Test
  fun `decodes inside a class`() =
    assertEquals(
      expected = Basket(listOf(Fruit.Apple, Fruit.Unknown)),
      actual = Json.decodeFromString<Basket>("""{"fruit":["Apple","Orange"]}"""),
    )

  private fun decode(value: String): Fruit = Json.decodeFromString("\"$value\"")
}

// The same enum without @Serializable(with = ...), relying on the generated `$serializer`
class VegTest {
  @Test fun `decodes by name`() = assertEquals(expected = Veg.Carrot, actual = decode("Carrot"))

  @Test
  fun `decodes by serial name`() = assertEquals(expected = Veg.Potato, actual = decode("spud"))

  @Test
  fun `decodes by alternative name`() =
    assertEquals(expected = Veg.Eggplant, actual = decode("aubergine"))

  @Test
  fun `decodes unknown value as fallback`() =
    assertEquals(expected = Veg.Unknown, actual = decode("Turnip"))

  @Test
  fun `encodes by serial name`() =
    assertEquals(expected = "\"spud\"", actual = Json.encodeToString(Veg.Potato))

  @Test
  fun `decodes inside a class`() =
    assertEquals(
      expected = Crate(listOf(Veg.Carrot, Veg.Unknown)),
      actual = Json.decodeFromString<Crate>("""{"veg":["Carrot","Turnip"]}"""),
    )

  private fun decode(value: String): Veg = Json.decodeFromString("\"$value\"")
}
