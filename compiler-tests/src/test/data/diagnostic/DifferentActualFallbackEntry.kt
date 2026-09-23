// LANGUAGE: +MultiPlatformProjects

// MODULE: common
// FILE: common.kt
import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable
expect enum class Weather {
  Sunny,
  @Fallback Unknown,
  Other,
}

// MODULE: platform()()(common)
// FILE: platform.kt
import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable
actual enum class Weather {
  Sunny,
  Unknown,
  @Fallback <!DIFFERENT_ACTUAL_FALLBACK_ENTRY!>Other<!>,
}
