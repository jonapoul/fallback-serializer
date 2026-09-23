import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

@Serializable
enum class Weather {
  Sunny,
  Unknown,
}

fun box(): String {
  val nested = Weather::class.java.declaredClasses.map { it.simpleName }
  assertFalse("\$serializer" in nested, "Unexpected nested classes: $nested")
  assertFailsWith<SerializationException> { Json.decodeFromString<Weather>("\"Hail\"") }
  return "OK"
}
