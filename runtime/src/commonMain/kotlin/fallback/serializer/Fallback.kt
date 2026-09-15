package fallback.serializer

/**
 * Marks the enum entry that unrecognised strings decode to. The enum must also be annotated with
 * `@Serializable(with = <Enum>.FallbackSerializer::class)`.
 */
@Target(AnnotationTarget.PROPERTY) public annotation class Fallback
