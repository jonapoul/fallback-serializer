import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@SerialName("fruit")
@Serializable(with = Fruit.FallbackSerializer::class)
enum class Fruit {
  Apple,
  @Fallback Unknown,
}

fun box(): String {
  assertEquals("fruit", Fruit.FallbackSerializer.descriptor.serialName)
  assertEquals("fruit.Apple", Fruit.FallbackSerializer.descriptor.getElementDescriptor(0).serialName)
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  return "OK"
}
