// LANGUAGE: +MultiPlatformProjects

// MODULE: common
// FILE: common.kt
import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
expect enum class Platform {
  Ios,
  @Fallback Other,
}

fun decodeInCommon(value: String): Platform = Json.decodeFromString<Platform>(value)

// MODULE: platform()()(common)
// FILE: platform.kt
import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
actual enum class Platform {
  Ios,
  @Fallback Other,
}

fun box(): String {
  assertEquals("Platform", Platform.serializer().descriptor.serialName)
  assertEquals(Platform.Ios, Json.decodeFromString<Platform>("\"Ios\""))
  assertEquals(Platform.Other, Json.decodeFromString<Platform>("\"Watch\""))
  assertEquals(Platform.Other, decodeInCommon("\"Watch\""))
  return "OK"
}
