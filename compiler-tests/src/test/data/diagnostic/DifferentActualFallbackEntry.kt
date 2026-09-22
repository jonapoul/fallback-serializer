// RENDER_DIAGNOSTICS_FULL_TEXT
// LANGUAGE: +MultiPlatformProjects

// MODULE: common
// FILE: common.kt
import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable
expect enum class Fruit {
  Apple,
  @Fallback Unknown,
  Other,
}

// MODULE: platform()()(common)
// FILE: platform.kt
import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable
actual enum class Fruit {
  Apple,
  Unknown,
  @Fallback <!DIFFERENT_ACTUAL_FALLBACK_ENTRY!>Other<!>,
}
