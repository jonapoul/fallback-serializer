package fallback.serializer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.protobuf.ProtoBuf
import nl.adaptivity.xmlutil.serialization.XML

// Values are encoded with a newer version of the enum, then decoded with the older one
@OptIn(ExperimentalSerializationApi::class)
class FormatsTest {
  private enum class Fruit {
    Apple,
    Unknown,
  }

  private enum class ServerFruit {
    Apple,
    Unknown,
    Orange,
  }

  private val client =
    FallbackEnumSerializer<Fruit>(serialName = "Fruit", fallbackValue = Fruit.Unknown)
  private val server =
    FallbackEnumSerializer<ServerFruit>(serialName = "Fruit", fallbackValue = ServerFruit.Unknown)

  @Test fun `decodes unknown cbor value as fallback`() = checkBinary(Cbor)

  // ProtoBuf encodes enums by index rather than by name
  @Test fun `decodes unknown protobuf value as fallback`() = checkBinary(ProtoBuf)

  @Test fun `decodes unknown xml value as fallback`() = checkString(XML.v1)

  private fun checkBinary(format: BinaryFormat) {
    assertEquals(
      expected = Fruit.Unknown,
      actual =
        format.decodeFromByteArray(client, format.encodeToByteArray(server, ServerFruit.Orange)),
    )
    assertEquals(
      expected = listOf(Fruit.Unknown, Fruit.Apple),
      actual =
        format.decodeFromByteArray(
          ListSerializer(client),
          format.encodeToByteArray(
            ListSerializer(server),
            listOf(ServerFruit.Orange, ServerFruit.Apple),
          ),
        ),
    )
  }

  private fun checkString(format: StringFormat) {
    assertEquals(
      expected = Fruit.Unknown,
      actual = format.decodeFromString(client, format.encodeToString(server, ServerFruit.Orange)),
    )
    assertEquals(
      expected = listOf(Fruit.Unknown, Fruit.Apple),
      actual =
        format.decodeFromString(
          ListSerializer(client),
          format.encodeToString(
            ListSerializer(server),
            listOf(ServerFruit.Orange, ServerFruit.Apple),
          ),
        ),
    )
  }
}
