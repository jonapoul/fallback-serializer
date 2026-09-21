// RENDER_DIAGNOSTICS_FULL_TEXT

import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable
enum class Fruit {
  Apple,
  @Fallback Unknown,
  @Fallback <!MULTIPLE_FALLBACK_ENTRIES!>Other<!>,
}
