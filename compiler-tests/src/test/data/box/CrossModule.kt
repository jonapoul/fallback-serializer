// MODULE: lib
// FILE: lib.kt
package lib

import fallback.serializer.Fallback
import kotlinx.serialization.Serializable

@Serializable
enum class HttpMethod {
  GET,
  POST,
  @Fallback Other,
}

// MODULE: main(lib)
// FILE: main.kt
import fallback.serializer.FallbackEnumSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.assertIs
import lib.HttpMethod

@Serializable data class Request(val method: HttpMethod)

// The enum is compiled in another module, so its serializer is only found from the compiled class
fun box(): String {
  assertIs<FallbackEnumSerializer<*>>(HttpMethod.serializer())
  assertEquals(HttpMethod.Other, Json.decodeFromString<HttpMethod>("\"PATCH\""))
  assertEquals(Request(HttpMethod.Other), Json.decodeFromString<Request>("""{"method":"PATCH"}"""))
  return "OK"
}
