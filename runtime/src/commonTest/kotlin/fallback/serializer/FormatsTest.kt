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
  private enum class HttpMethod {
    GET,
    Other,
  }

  private enum class ServerHttpMethod {
    GET,
    Other,
    PATCH,
  }

  private val client =
    FallbackEnumSerializer<HttpMethod>(serialName = "HttpMethod", fallbackValue = HttpMethod.Other)
  private val server =
    FallbackEnumSerializer<ServerHttpMethod>(
      serialName = "HttpMethod",
      fallbackValue = ServerHttpMethod.Other,
    )

  @Test fun `decodes unknown cbor value as fallback`() = checkBinary(Cbor)

  // ProtoBuf encodes enums by index rather than by name
  @Test fun `decodes unknown protobuf value as fallback`() = checkBinary(ProtoBuf)

  @Test fun `decodes unknown xml value as fallback`() = checkString(XML.v1)

  private fun checkBinary(format: BinaryFormat) {
    assertEquals(
      expected = HttpMethod.Other,
      actual =
        format.decodeFromByteArray(
          client,
          format.encodeToByteArray(server, ServerHttpMethod.PATCH),
        ),
    )
    assertEquals(
      expected = listOf(HttpMethod.Other, HttpMethod.GET),
      actual =
        format.decodeFromByteArray(
          ListSerializer(client),
          format.encodeToByteArray(
            ListSerializer(server),
            listOf(ServerHttpMethod.PATCH, ServerHttpMethod.GET),
          ),
        ),
    )
  }

  private fun checkString(format: StringFormat) {
    assertEquals(
      expected = HttpMethod.Other,
      actual =
        format.decodeFromString(client, format.encodeToString(server, ServerHttpMethod.PATCH)),
    )
    assertEquals(
      expected = listOf(HttpMethod.Other, HttpMethod.GET),
      actual =
        format.decodeFromString(
          ListSerializer(client),
          format.encodeToString(
            ListSerializer(server),
            listOf(ServerHttpMethod.PATCH, ServerHttpMethod.GET),
          ),
        ),
    )
  }
}
