package sample

import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable
actual enum class Colour {
  Red,
  @Fallback Unknown,
}
