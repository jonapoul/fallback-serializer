import fallback.serializer.Fallback
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@SerialName("fruit")
@Serializable
enum class Fruit {
  Apple,
  @Fallback Unknown,
}

fun box(): String {
  assertEquals("fruit", Fruit.serializer().descriptor.serialName)
  assertEquals("fruit.Apple", Fruit.serializer().descriptor.getElementDescriptor(0).serialName)
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  return "OK"
}
