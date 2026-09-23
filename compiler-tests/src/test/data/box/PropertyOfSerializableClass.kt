import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable
enum class Permission {
  Read,
  Write,
  @Fallback None,
}

@Serializable data class User(val role: Permission, val extras: List<Permission>)

fun box(): String {
  val user = Json.decodeFromString<User>("""{"role":"Owner","extras":["Read","Delete"]}""")
  assertEquals(User(Permission.None, listOf(Permission.Read, Permission.None)), user)
  return "OK"
}
