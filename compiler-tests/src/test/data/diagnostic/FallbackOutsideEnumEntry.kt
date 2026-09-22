// RENDER_DIAGNOSTICS_FULL_TEXT

import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable data class Order(<!FALLBACK_OUTSIDE_ENUM_ENTRY!>@Fallback<!> val name: String)

class Basket {
  <!FALLBACK_OUTSIDE_ENUM_ENTRY!>@Fallback<!> val size = 1
}

<!FALLBACK_OUTSIDE_ENUM_ENTRY!>@Fallback<!> val topLevel = ""

// Only enum entries count, so no serializer is generated for this enum
@Serializable
enum class Fruit {
  Apple;

  <!FALLBACK_OUTSIDE_ENUM_ENTRY!>@Fallback<!> val size = 1
}

val serializer = Fruit.<!UNRESOLVED_REFERENCE!>`$serializer`<!>
