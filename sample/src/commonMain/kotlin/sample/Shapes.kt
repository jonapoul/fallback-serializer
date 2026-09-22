@file:OptIn(ExperimentalSerializationApi::class)

package sample

import fallback.serializer.Fallback
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// kotlinx.serialization adds serializer() to a declared companion instead of generating one
@Serializable
enum class Veg {
  Carrot,
  @Fallback Unknown;

  companion object
}

class Crate {
  @Serializable
  enum class Size {
    Small,
    @Fallback Unknown,
  }
}

@Serializable
private enum class Secret {
  Apple,
  @Fallback Unknown,
}

fun decodeSecret(json: String): String = Json.decodeFromString<Secret>(json).name

@SerialInfo @Target(AnnotationTarget.CLASS) annotation class Tag(val value: String)

@Tag("tagged")
@Serializable
enum class Tagged {
  Apple,
  @Fallback Unknown,
}

// Each platform source set has the actual enum
@Serializable
expect enum class Colour {
  Red,
  @Fallback Unknown,
}
