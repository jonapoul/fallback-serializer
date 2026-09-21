package fallback.serializer

/**
 * Marks the enum entry that unrecognised strings decode to. The enum must also be annotated with a
 * plain `@Serializable`, without a `with` argument.
 */
@Target(AnnotationTarget.PROPERTY) public annotation class Fallback
