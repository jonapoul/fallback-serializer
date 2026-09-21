import fallback.serializer.Fallback
import fallback.serializer.FallbackEnumSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertIs

// kotlinx.serialization adds serializer() to a declared companion instead of generating one
@Serializable
enum class Fruit {
  Apple,
  @Fallback Unknown;

  companion object
}

@Serializable
enum class Veg {
  Carrot,
  @Fallback Unknown;

  companion object Named
}

fun box(): String {
  assertIs<FallbackEnumSerializer<*>>(Fruit.serializer())
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  assertIs<FallbackEnumSerializer<*>>(Veg.serializer())
  assertEquals(Veg.Unknown, Json.decodeFromString<Veg>("\"Orange\""))
  return "OK"
}
