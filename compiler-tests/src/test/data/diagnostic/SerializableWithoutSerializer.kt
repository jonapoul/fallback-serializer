// RENDER_DIAGNOSTICS_FULL_TEXT

import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

<!OTHER_ERROR_WITH_REASON!>@Serializable
enum class Fruit {
  Apple,
  @Fallback Unknown,
}<!>
