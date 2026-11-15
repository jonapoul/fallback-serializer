// Opting in by hand compiles, for builds that add the compiler plugin without Gradle
@file:OptIn(FallbackPluginApplied::class)

import fallback.serializer.Fallback
import fallback.serializer.FallbackPluginApplied
import kotlinx.serialization.Serializable

@Serializable
enum class Permission {
  Read,
  @Fallback None,
}
