// RENDER_DIAGNOSTICS_FULL_TEXT

import fallback.serializer.Fallback

<!MISSING_FALLBACK_SERIALIZER!>enum class Fruit<!> {
  Apple,
  @Fallback Unknown,
}
