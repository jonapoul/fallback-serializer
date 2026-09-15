// LANGUAGE: +MultiPlatformProjects

// MODULE: common
// FILE: common.kt
import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable(with = Fruit.FallbackSerializer::class)
expect enum class Fruit {
  Apple,
  @Fallback Unknown,
}

fun decodeInCommon(value: String): Fruit = Json.decodeFromString<Fruit>(value)

// MODULE: platform()()(common)
// FILE: platform.kt
import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable(with = Fruit.FallbackSerializer::class)
actual enum class Fruit {
  Apple,
  @Fallback Unknown,
}

fun box(): String {
  assertEquals("Fruit", Fruit.FallbackSerializer.descriptor.serialName)
  assertEquals(Fruit.Apple, Json.decodeFromString<Fruit>("\"Apple\""))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  assertEquals(Fruit.Unknown, decodeInCommon("\"Orange\""))
  return "OK"
}
