import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

interface Prioritised {
  val priority: Int
}

@Serializable
enum class LogLevel(override val priority: Int) : Prioritised {
  Debug(10),
  Info(20),
  @Fallback Unrecognised(0),
}

fun box(): String {
  assertEquals(LogLevel.Info, Json.decodeFromString<LogLevel>("\"Info\""))
  assertEquals(LogLevel.Unrecognised, Json.decodeFromString<LogLevel>("\"Trace\""))
  assertEquals(0, Json.decodeFromString<LogLevel>("\"Trace\"").priority)
  return "OK"
}
