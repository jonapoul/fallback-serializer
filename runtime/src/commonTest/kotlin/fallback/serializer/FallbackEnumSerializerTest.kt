package fallback.serializer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json

class FallbackEnumSerializerTest {
  private enum class Fruit {
    Apple,
    @SerialName("cherry_pie") Cherry,
    Unknown,
  }

  // Hand-written equivalent of the compiler plugin's output
  private object FruitSerializer :
    FallbackEnumSerializer<Fruit>(
      serialName = "Fruit",
      values = Fruit.entries.toTypedArray(),
      valueSerialNames = mapOf(Fruit.Cherry to "cherry_pie"),
      fallbackValue = Unknown,
    )

  @Test fun `decodes by name`() = assertEquals(expected = Apple, actual = decode("Apple"))

  @Test
  fun `decodes by serial name`() = assertEquals(expected = Cherry, actual = decode("cherry_pie"))

  @Test
  fun `decodes unrecognised value as fallback`() =
    assertEquals(expected = Unknown, actual = decode("Orange"))

  @Test
  fun `encodes by serial name`() =
    assertEquals(expected = "\"cherry_pie\"", actual = encode(Cherry))

  @Test
  fun `encodes fallback by name`() =
    assertEquals(expected = "\"Unknown\"", actual = encode(Unknown))

  @Test
  fun `descriptor uses serial name`() =
    assertEquals(expected = "Fruit", actual = FruitSerializer.descriptor.serialName)

  @Test
  fun `invoke uses defaults`() {
    val serializer = FallbackEnumSerializer<Fruit>(serialName = "Fruit", fallbackValue = Unknown)
    assertEquals(expected = Fruit.Cherry, actual = Json.decodeFromString(serializer, "\"Cherry\""))
    assertEquals(
      expected = Fruit.Unknown,
      actual = Json.decodeFromString(serializer, "\"cherry_pie\""),
    )
  }

  private fun encode(value: Fruit): String = Json.encodeToString(FruitSerializer, value)

  private fun decode(value: String): Fruit = Json.decodeFromString(FruitSerializer, "\"$value\"")
}
