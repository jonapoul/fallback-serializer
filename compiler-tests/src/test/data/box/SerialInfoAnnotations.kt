@file:OptIn(ExperimentalSerializationApi::class)

import fallback.serializer.Fallback
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import kotlin.test.assertEquals

@SerialInfo
@Target(AnnotationTarget.CLASS)
annotation class Tag(val value: String)

@Tag("fruit")
@Serializable(with = Fruit.FallbackSerializer::class)
enum class Fruit {
  @JsonNames("apple", "APPLE") Apple,
  @SerialName("cherry_pie") @JsonNames("cherry") Cherry,
  @Fallback Unknown,
}

fun box(): String {
  val descriptor = Fruit.FallbackSerializer.descriptor
  assertEquals(listOf<Annotation>(Tag("fruit")), descriptor.annotations)
  assertEquals(listOf<Annotation>(JsonNames("apple", "APPLE")), descriptor.getElementAnnotations(0))
  // Annotations without @SerialInfo, like @SerialName and @Fallback, aren't included
  assertEquals(listOf<Annotation>(JsonNames("cherry")), descriptor.getElementAnnotations(1))
  assertEquals(emptyList(), descriptor.getElementAnnotations(2))

  assertEquals(Fruit.Apple, Json.decodeFromString<Fruit>("\"APPLE\""))
  assertEquals(Fruit.Cherry, Json.decodeFromString<Fruit>("\"cherry\""))
  assertEquals(Fruit.Unknown, Json.decodeFromString<Fruit>("\"Orange\""))
  return "OK"
}
