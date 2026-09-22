package fallback.serializer.compiler

// Runs the same diagnostic tests as AbstractDiagnosticTest, but with our plugin before
// kotlinx.serialization's
open class AbstractFallbackFirstDiagnosticTest : AbstractDiagnosticTest() {
  override val pluginOrder: PluginOrder
    get() = PluginOrder.FallbackFirst
}
