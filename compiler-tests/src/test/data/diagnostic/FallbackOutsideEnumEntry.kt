// RENDER_DIAGNOSTICS_FULL_TEXT

import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable data class Order(<!OTHER_ERROR_WITH_REASON!>@Fallback<!> val name: String)

class Basket {
  <!OTHER_ERROR_WITH_REASON!>@Fallback<!> val size = 1
}

<!OTHER_ERROR_WITH_REASON!>@Fallback<!> val topLevel = ""
