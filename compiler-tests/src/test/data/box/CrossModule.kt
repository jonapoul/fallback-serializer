// MODULE: lib
// FILE: lib.kt
package lib

import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable
enum class Fruit {
  Apple,
  @Fallback Unknown,
}

// MODULE: main(lib)
// FILE: main.kt
import fallback.serializer.FallbackEnumSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertIs
import lib.Fruit

@Serializable data class Order(val fruit: Fruit)

// The enum is compiled in another module, so its serializer is only found from the compiled class
fun box(): String {
  assertIs<FallbackEnumSerializer<*>>(Fruit.serializer())
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  assertEquals(Order(Fruit.Unknown), Json.decodeFromString<Order>("""{"fruit":"Orange"}"""))
  return "OK"
}
