import kotlinx.serialization.Serializable
import kotlin.test.assertFalse

@Serializable
enum class Fruit {
  Apple,
  Unknown,
}

fun box(): String {
  val nested = Fruit::class.java.declaredClasses.map { it.simpleName }
  assertFalse("FallbackSerializer" in nested, "Unexpected nested classes: $nested")
  return "OK"
}
