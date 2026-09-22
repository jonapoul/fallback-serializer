@file:OptIn(ExperimentalSerializationApi::class)

import fallback.serializer.Fallback
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import kotlin.test.assertEquals

@Serializable
enum class Fruit {
  @JsonNames("pomme") Apple,
  @SerialName("cherry_pie") Cherry,
  @Fallback Unknown,
}

fun box(): String {
  val caseInsensitive = Json { decodeEnumsCaseInsensitive = true }
  assertEquals(Fruit.Apple, caseInsensitive.decodeFromString<Fruit>("\"APPLE\""))
  assertEquals(Fruit.Apple, caseInsensitive.decodeFromString<Fruit>("\"POMME\""))
  assertEquals(Fruit.Cherry, caseInsensitive.decodeFromString<Fruit>("\"Cherry_Pie\""))
  assertEquals(Fruit.Unknown, caseInsensitive.decodeFromString<Fruit>("\"Orange\""))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"APPLE\""))

  val noAlternativeNames = Json { useAlternativeNames = false }
  assertEquals(Fruit.Apple, Json.decodeFromString<Fruit>("\"pomme\""))
  assertEquals(Fruit.Unknown, noAlternativeNames.decodeFromString<Fruit>("\"pomme\""))
  return "OK"
}
