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
  Apple,
  @Fallback @SerialName("unknown") @JsonNames("other") Unknown,
}

fun box(): String {
  assertEquals("\"unknown\"", Json.encodeToString(Fruit.Unknown))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"unknown\""))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"other\""))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  assertEquals(listOf<Annotation>(JsonNames("other")), Fruit.serializer().descriptor.getElementAnnotations(1))
  return "OK"
}
