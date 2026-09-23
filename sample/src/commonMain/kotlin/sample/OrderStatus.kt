@file:OptIn(ExperimentalSerializationApi::class)

package sample

import fallback.serializer.Fallback
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@SerialName("order_status")
@Serializable
enum class OrderStatus {
  Pending,
  @SerialName("in_transit") Shipped,
  @JsonNames("complete") Delivered,
  @Fallback Unknown,
}

@Serializable data class Order(val history: List<OrderStatus>)
