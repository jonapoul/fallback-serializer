package fallback.serializer.compiler

// Runs the same box tests as AbstractBoxTest, but with our plugin before kotlinx.serialization's
open class AbstractFallbackFirstBoxTest : AbstractBoxTest() {
  override val pluginOrder: PluginOrder
    get() = PluginOrder.FallbackFirst
}
