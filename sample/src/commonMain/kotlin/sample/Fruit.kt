@file:OptIn(ExperimentalSerializationApi::class)

package sample

import fallback.serializer.Fallback
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@SerialName("fruit")
@Serializable
enum class Fruit {
  Apple,
  @SerialName("cherry_pie") Cherry,
  @JsonNames("banana") Banana,
  @Fallback Unknown,
}

@Serializable data class Basket(val fruit: List<Fruit>)
