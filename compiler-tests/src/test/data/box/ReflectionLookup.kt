@file:OptIn(InternalSerializationApi::class)

import fallback.serializer.Fallback
import fallback.serializer.FallbackEnumSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Serializable
enum class Fruit {
  Apple,
  @Fallback Unknown,
}

// Serializers looked up at runtime, like when the type isn't known at compile time
fun box(): String {
  assertIs<FallbackEnumSerializer<*>>(Fruit::class.serializer())
  assertIs<FallbackEnumSerializer<*>>(serializer(Fruit::class.java))
  assertIs<FallbackEnumSerializer<*>>(serializer(typeOf<Fruit>()))
  assertEquals(Fruit.Unknown, Json.decodeFromString(serializer(typeOf<Fruit?>()), "\"Orange\""))
  return "OK"
}
