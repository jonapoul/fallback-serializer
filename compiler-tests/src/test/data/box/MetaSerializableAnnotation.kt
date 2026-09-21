@file:OptIn(ExperimentalSerializationApi::class)

import fallback.serializer.Fallback
import fallback.serializer.FallbackEnumSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MetaSerializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertIs

@MetaSerializable annotation class ApiModel

// An annotation marked with @MetaSerializable counts as @Serializable
@ApiModel
enum class Fruit {
  Apple,
  @Fallback Unknown,
}

fun box(): String {
  assertIs<FallbackEnumSerializer<*>>(Fruit.serializer())
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  return "OK"
}
