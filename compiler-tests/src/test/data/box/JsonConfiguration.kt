@file:OptIn(ExperimentalSerializationApi::class)

import fallback.serializer.Fallback
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import kotlin.test.assertEquals

@Serializable
enum class Platform {
  @SerialName("android") Android,
  @SerialName("ios") Ios,
  @JsonNames("browser") Web,
  @Fallback Other,
}

fun box(): String {
  val caseInsensitive = Json { decodeEnumsCaseInsensitive = true }
  assertEquals(Platform.Web, caseInsensitive.decodeFromString<Platform>("\"WEB\""))
  assertEquals(Platform.Web, caseInsensitive.decodeFromString<Platform>("\"BROWSER\""))
  assertEquals(Platform.Ios, caseInsensitive.decodeFromString<Platform>("\"IOS\""))
  assertEquals(Platform.Other, caseInsensitive.decodeFromString<Platform>("\"Watch\""))
  assertEquals(Platform.Other, Json.decodeFromString<Platform>("\"WEB\""))

  val noAlternativeNames = Json { useAlternativeNames = false }
  assertEquals(Platform.Web, Json.decodeFromString<Platform>("\"browser\""))
  assertEquals(Platform.Other, noAlternativeNames.decodeFromString<Platform>("\"browser\""))
  return "OK"
}
