package fallback.serializer.compiler

import fallback.serializer.compiler.compat.CompatContext
import fallback.serializer.compiler.compat.k2220.K2220Compat
import fallback.serializer.compiler.compat.k240.K240Compat
import org.jetbrains.kotlin.config.KotlinCompilerVersion

private val Kotlin240 = KotlinVersion(major = 2, minor = 4, patch = 0)

// Not KotlinCompilerVersion.VERSION, which is a constant and would be inlined at build time
internal val compat: CompatContext by lazy {
  compatFor(checkNotNull(KotlinCompilerVersion.getVersion()))
}

internal fun compatFor(version: String): CompatContext {
  val (major, minor, patch) =
    Regex("""^(\d+)\.(\d+)\.(\d+)""").find(version)?.destructured
      ?: error("Unknown Kotlin compiler version '$version'")
  val kotlin = KotlinVersion(major = major.toInt(), minor = minor.toInt(), patch = patch.toInt())
  return if (kotlin >= Kotlin240) K240Compat else K2220Compat
}
