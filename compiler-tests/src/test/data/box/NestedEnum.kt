package test

import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

class Basket {
  @Serializable
  enum class Fruit {
    Apple,
    @Fallback Unknown,
  }
}

fun box(): String {
  assertEquals("test.Basket.Fruit", Basket.Fruit.serializer().descriptor.serialName)
  assertEquals(Basket.Fruit.Apple, Json.decodeFromString<Basket.Fruit>("\"Apple\""))
  assertEquals(Basket.Fruit.Unknown, Json.decodeFromString<Basket.Fruit>("\"Orange\""))
  return "OK"
}
