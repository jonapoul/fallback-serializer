import fallback.serializer.Fallback
import fallback.serializer.FallbackEnumSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Serializable
private enum class PrivateFruit {
  Apple,
  @Fallback Unknown,
}

@Serializable
internal enum class InternalFruit {
  Apple,
  @Fallback Unknown,
}

class Basket {
  @Serializable
  private enum class NestedFruit {
    Apple,
    @Fallback Unknown,
  }

  fun check() {
    assertIs<FallbackEnumSerializer<*>>(NestedFruit.serializer())
    assertEquals(NestedFruit.Unknown, Json.decodeFromString<NestedFruit>("\"Orange\""))
  }
}

fun box(): String {
  assertIs<FallbackEnumSerializer<*>>(PrivateFruit.serializer())
  assertEquals(PrivateFruit.Unknown, Json.decodeFromString<PrivateFruit>("\"Orange\""))
  assertIs<FallbackEnumSerializer<*>>(InternalFruit.serializer())
  assertEquals(InternalFruit.Unknown, Json.decodeFromString<InternalFruit>("\"Orange\""))
  Basket().check()
  return "OK"
}
