package fallback.serializer

/**
 * Makes `@Fallback` fail to compile unless the `dev.jonpoulton.fallbackserializer` Gradle plugin is
 * applied, since the annotation does nothing without the compiler plugin. The Gradle plugin opts in
 * to this for every compilation. Builds that add the compiler plugin some other way should pass
 * `-opt-in=fallback.serializer.FallbackPluginApplied` to the compiler.
 */
@RequiresOptIn(
  message =
    "@Fallback does nothing without the compiler plugin. Apply the " +
      "dev.jonpoulton.fallbackserializer Gradle plugin.",
  level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS)
public annotation class FallbackPluginApplied
