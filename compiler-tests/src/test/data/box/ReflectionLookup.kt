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
enum class LogLevel {
  Warn,
  Error,
  @Fallback Unrecognised,
}

// Serializers looked up at runtime, like when the type isn't known at compile time
fun box(): String {
  assertIs<FallbackEnumSerializer<*>>(LogLevel::class.serializer())
  assertIs<FallbackEnumSerializer<*>>(serializer(LogLevel::class.java))
  assertIs<FallbackEnumSerializer<*>>(serializer(typeOf<LogLevel>()))
  assertEquals(LogLevel.Unrecognised, Json.decodeFromString(serializer(typeOf<LogLevel?>()), "\"Trace\""))
  return "OK"
}
