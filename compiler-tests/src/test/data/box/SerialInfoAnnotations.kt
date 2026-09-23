@file:OptIn(ExperimentalSerializationApi::class)

import fallback.serializer.Fallback
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import kotlin.test.assertEquals

@SerialInfo
@Target(AnnotationTarget.CLASS)
annotation class Tag(val value: String)

@Tag("payment")
@Serializable
enum class PaymentMethod {
  @JsonNames("card", "CARD") Card,
  @SerialName("bank_transfer") @JsonNames("bank") BankTransfer,
  @Fallback Unsupported,
}

fun box(): String {
  val descriptor = PaymentMethod.serializer().descriptor
  assertEquals(listOf<Annotation>(Tag("payment")), descriptor.annotations)
  assertEquals(listOf<Annotation>(JsonNames("card", "CARD")), descriptor.getElementAnnotations(0))
  // Annotations without @SerialInfo, like @SerialName and @Fallback, aren't included
  assertEquals(listOf<Annotation>(JsonNames("bank")), descriptor.getElementAnnotations(1))
  assertEquals(emptyList(), descriptor.getElementAnnotations(2))

  assertEquals(PaymentMethod.Card, Json.decodeFromString<PaymentMethod>("\"CARD\""))
  assertEquals(PaymentMethod.BankTransfer, Json.decodeFromString<PaymentMethod>("\"bank\""))
  assertEquals(PaymentMethod.Unsupported, Json.decodeFromString<PaymentMethod>("\"Crypto\""))
  return "OK"
}
