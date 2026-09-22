import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

@Serializable
enum class Fruit {
  Apple,
  Unknown,
}

fun box(): String {
  val nested = Fruit::class.java.declaredClasses.map { it.simpleName }
  assertFalse("\$serializer" in nested, "Unexpected nested classes: $nested")
  assertFailsWith<SerializationException> { Json.decodeFromString<Fruit>("\"Orange\"") }
  return "OK"
}
