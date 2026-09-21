// LANGUAGE: +MultiPlatformProjects

// MODULE: common
// FILE: common.kt
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
expect enum class Fruit {
  Apple,
  Unknown,
}

// Common code can't see the generated serializer, so this checks it's still used at runtime
fun decodeInCommon(value: String): Fruit = Json.decodeFromString<Fruit>(value)

// MODULE: platform()()(common)
// FILE: platform.kt
import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

// Only the actual enum needs the @Fallback entry
@Serializable
actual enum class Fruit {
  Apple,
  @Fallback Unknown,
}

fun box(): String {
  assertEquals(Fruit.Apple, Json.decodeFromString<Fruit>("\"Apple\""))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  assertEquals(Fruit.Unknown, decodeInCommon("\"Orange\""))
  return "OK"
}
