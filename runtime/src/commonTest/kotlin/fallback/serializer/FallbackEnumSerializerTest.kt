package fallback.serializer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonNull

@OptIn(ExperimentalSerializationApi::class)
class FallbackEnumSerializerTest {
  private enum class OrderStatus {
    Pending,
    @SerialName("in_transit") Shipped,
    Unknown,
  }

  // Hand-written equivalent of the compiler plugin's output
  private object StatusSerializer :
    FallbackEnumSerializer<OrderStatus>(
      serialName = "OrderStatus",
      values = OrderStatus.entries.toTypedArray(),
      valueSerialNames = mapOf(OrderStatus.Shipped to "in_transit"),
      fallbackValue = Unknown,
    )

  @Test fun `decodes by name`() = assertEquals(expected = Pending, actual = decode("Pending"))

  @Test
  fun `decodes by serial name`() = assertEquals(expected = Shipped, actual = decode("in_transit"))

  @Test
  fun `decodes unrecognised value as fallback`() =
    assertEquals(expected = Unknown, actual = decode("Refunded"))

  @Test
  fun `decodes empty input as fallback`() =
    assertEquals(expected = Unknown, actual = Json.decodeFromString(StatusSerializer, ""))

  @Test
  fun `decodes json null as fallback`() =
    assertEquals(
      expected = Unknown,
      actual = Json.decodeFromJsonElement(StatusSerializer, JsonNull),
    )

  // The fallback only covers unknown strings. Decoding from a string fails partway through any
  // other value, but a JsonElement has already been parsed, so there's nothing left half-read.
  @Test
  fun `fails to decode non-string values in a string`() {
    for (value in listOf("123", "true", "null", "{\"a\":1}", "[\"Pending\"]")) {
      assertFailsWith<SerializationException>(value) {
        Json.decodeFromString(ListSerializer(StatusSerializer), "[$value,\"Pending\"]")
      }
    }
  }

  @Test
  fun `decodes non-string json elements as fallback`() {
    for (value in listOf("123", "true", "null", "{\"a\":1}", "[\"Pending\"]")) {
      assertEquals(
        expected = listOf(Unknown, Pending),
        actual =
          Json.decodeFromJsonElement(
            ListSerializer(StatusSerializer),
            Json.parseToJsonElement("[$value,\"Pending\"]"),
          ),
        message = value,
      )
    }
  }

  @Test
  fun `encodes by serial name`() =
    assertEquals(expected = "\"in_transit\"", actual = encode(Shipped))

  @Test
  fun `encodes fallback by name`() =
    assertEquals(expected = "\"Unknown\"", actual = encode(Unknown))

  @Test
  fun `descriptor uses serial name`() =
    assertEquals(expected = "OrderStatus", actual = StatusSerializer.descriptor.serialName)

  @Test
  fun `descriptor has an element per value`() {
    val descriptor = StatusSerializer.descriptor
    assertEquals(expected = 3, actual = descriptor.elementsCount)
    assertEquals(
      expected = listOf("Pending", "in_transit", "Unknown"),
      actual = List(descriptor.elementsCount, descriptor::getElementName),
    )
  }

  @Test
  fun `fails to encode a value missing from values`() {
    val serializer =
      FallbackEnumSerializer(
        serialName = "OrderStatus",
        values = arrayOf(OrderStatus.Pending, OrderStatus.Unknown),
        valueSerialNames = emptyMap(),
        fallbackValue = OrderStatus.Unknown,
      )
    val error =
      assertFailsWith<IllegalStateException> {
        Json.encodeToString(serializer, OrderStatus.Shipped)
      }
    assertEquals(
      expected = "Shipped is not a valid enum OrderStatus, must be one of [Pending, Unknown]",
      actual = error.message,
    )
  }

  @Test
  fun `toString includes serial name`() =
    assertEquals(
      expected = "FallbackEnumSerializer<OrderStatus>",
      actual = StatusSerializer.toString(),
    )

  @Test
  fun `decodes by alternative name`() {
    val serializer =
      FallbackEnumSerializer<OrderStatus>(
        serialName = "OrderStatus",
        fallbackValue = Unknown,
        valueAnnotations = mapOf(OrderStatus.Pending to listOf(JsonNames("pending"))),
      )
    assertEquals(
      expected = OrderStatus.Pending,
      actual = Json.decodeFromString(serializer, "\"pending\""),
    )
  }

  @Test
  fun `fails to decode when alternative names clash`() {
    val serializer =
      FallbackEnumSerializer<OrderStatus>(
        serialName = "OrderStatus",
        fallbackValue = Unknown,
        valueAnnotations = mapOf(OrderStatus.Pending to listOf(JsonNames("pending"))),
      )
    val json = Json { decodeEnumsCaseInsensitive = true }
    val error =
      assertFailsWith<SerializationException> { json.decodeFromString(serializer, "\"Pending\"") }

    assertEquals(
      expected = true,
      actual = error.message?.startsWith("The suggested name 'pending'"),
      message = error.message,
    )
  }

  @Test
  fun `descriptor has annotations`() {
    val serializer =
      FallbackEnumSerializer<OrderStatus>(
        serialName = "OrderStatus",
        fallbackValue = Unknown,
        valueAnnotations = mapOf(OrderStatus.Pending to listOf(JsonNames("pending"))),
        annotations = listOf(JsonNames("status")),
      )
    assertEquals(expected = listOf(JsonNames("status")), actual = serializer.descriptor.annotations)
    assertEquals(
      expected = listOf(JsonNames("pending")),
      actual = serializer.descriptor.getElementAnnotations(0),
    )
    assertEquals(expected = emptyList(), actual = serializer.descriptor.getElementAnnotations(1))
  }

  @Test
  fun `invoke uses defaults`() {
    val serializer =
      FallbackEnumSerializer<OrderStatus>(serialName = "OrderStatus", fallbackValue = Unknown)
    assertEquals(
      expected = OrderStatus.Shipped,
      actual = Json.decodeFromString(serializer, "\"Shipped\""),
    )
    assertEquals(
      expected = OrderStatus.Unknown,
      actual = Json.decodeFromString(serializer, "\"in_transit\""),
    )
  }

  private fun encode(value: OrderStatus): String = Json.encodeToString(StatusSerializer, value)

  private fun decode(value: String): OrderStatus =
    Json.decodeFromString(StatusSerializer, "\"$value\"")
}
