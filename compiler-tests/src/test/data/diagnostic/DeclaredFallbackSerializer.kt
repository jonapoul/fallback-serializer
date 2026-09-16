// RENDER_DIAGNOSTICS_FULL_TEXT

import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable(with = <!ARGUMENT_TYPE_MISMATCH!>Fruit.FallbackSerializer::class<!>)
enum class Fruit {
  Apple,
  @Fallback Unknown;

  <!OTHER_ERROR_WITH_REASON!>object FallbackSerializer<!>
}
