// LANGUAGE: +MultiPlatformProjects

// MODULE: common
// FILE: common.kt
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
expect enum class Platform {
  Android,
  Other,
}

// Common code can't see the generated serializer, so this checks it's still used at runtime
fun decodeInCommon(value: String): Platform = Json.decodeFromString<Platform>(value)

// MODULE: platform()()(common)
// FILE: platform.kt
import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

// Only the actual enum needs the @Fallback entry
@Serializable
actual enum class Platform {
  Android,
  @Fallback Other,
}

fun box(): String {
  assertEquals(Platform.Android, Json.decodeFromString<Platform>("\"Android\""))
  assertEquals(Platform.Other, Json.decodeFromString<Platform>("\"Watch\""))
  assertEquals(Platform.Other, decodeInCommon("\"Watch\""))
  return "OK"
}
