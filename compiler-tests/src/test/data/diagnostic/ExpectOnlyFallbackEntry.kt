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
}

// MODULE: platform()()(common)
// FILE: platform.kt
import kotlinx.serialization.Serializable

// The actual enum doesn't get the generated serializer, so it doesn't match the expect one
@Serializable
actual enum class <!NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS!>Fruit<!> {
  Apple,
  Unknown,
}
