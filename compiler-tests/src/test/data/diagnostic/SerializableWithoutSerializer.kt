// RENDER_DIAGNOSTICS_FULL_TEXT

import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable
<!MISSING_FALLBACK_SERIALIZER!>enum class Fruit<!> {
  Apple,
  @Fallback Unknown,
}
