import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable
enum class HttpMethod {
  GET,
  @Fallback Other,
  @Fallback <!MULTIPLE_FALLBACK_ENTRIES!>Unknown<!>,
  @Fallback <!MULTIPLE_FALLBACK_ENTRIES!>Custom<!>,
}
