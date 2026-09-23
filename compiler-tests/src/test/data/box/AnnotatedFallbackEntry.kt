@file:OptIn(ExperimentalSerializationApi::class)

import fallback.serializer.Fallback
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import kotlin.test.assertEquals

@Serializable
enum class PaymentMethod {
  Card,
  @Fallback @SerialName("unsupported") @JsonNames("other") Unsupported,
}

fun box(): String {
  assertEquals("\"unsupported\"", Json.encodeToString(PaymentMethod.Unsupported))
  assertEquals(PaymentMethod.Unsupported, Json.decodeFromString<PaymentMethod>("\"unsupported\""))
  assertEquals(PaymentMethod.Unsupported, Json.decodeFromString<PaymentMethod>("\"other\""))
  assertEquals(PaymentMethod.Unsupported, Json.decodeFromString<PaymentMethod>("\"Crypto\""))
  assertEquals(listOf<Annotation>(JsonNames("other")), PaymentMethod.serializer().descriptor.getElementAnnotations(1))
  return "OK"
}
