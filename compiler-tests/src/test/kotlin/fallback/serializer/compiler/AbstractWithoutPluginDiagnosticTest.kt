package fallback.serializer.compiler

// Compiles each file under src/test/data/withoutPlugin with our runtime on the classpath, but
// without our compiler plugin or its opt-in, as when the Gradle plugin isn't applied
open class AbstractWithoutPluginDiagnosticTest : AbstractDiagnosticTest() {
  override val hasFallbackPlugin: Boolean
    get() = false
}
