import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable
enum class Permission {
  Read,
  @<!OPT_IN_USAGE_ERROR!>Fallback<!> None,
}
