import fallback.serializer.Fallback
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals

@Serializable(with = Fruit.FallbackSerializer::class)
enum class Fruit {
  Apple,
  @Fallback Unknown,
}

fun box(): String {
  // Unknown values all decode to the same entry, so later ones replace earlier ones
  assertEquals(
    mapOf(Fruit.Apple to 0, Fruit.Unknown to 2),
    Json.decodeFromString<Map<Fruit, Int>>("""{"Apple":0,"Orange":1,"Mango":2}"""),
  )
  assertEquals(
    setOf(Fruit.Apple, Fruit.Unknown),
    Json.decodeFromString<Set<Fruit>>("""["Apple","Orange","Mango"]"""),
  )
  return "OK"
}
