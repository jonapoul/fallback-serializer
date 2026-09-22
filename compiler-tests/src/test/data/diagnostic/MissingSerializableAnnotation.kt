import fallback.serializer.Fallback

<!MISSING_SERIALIZABLE!>enum class Fruit<!> {
  Apple,
  @Fallback Unknown,
}
