// LANGUAGE: +MultiPlatformProjects

// MODULE: common
// FILE: common.kt
import kotlinx.serialization.Serializable

@Serializable
expect enum class Fruit {
  Apple,
  Unknown,
}

// MODULE: platform()()(common)
// FILE: platform.kt
import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

// Only the actual enum needs the @Fallback entry
@Serializable(with = Fruit.FallbackSerializer::class)
actual enum class Fruit {
  Apple,
  @Fallback Unknown,
}

fun box(): String {
  assertEquals(Fruit.Apple, Json.decodeFromString<Fruit>("\"Apple\""))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  return "OK"
}
