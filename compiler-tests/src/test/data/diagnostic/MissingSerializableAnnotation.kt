import fallback.serializer.Fallback

<!MISSING_SERIALIZABLE!>enum class Permission<!> {
  Read,
  @Fallback None,
}
