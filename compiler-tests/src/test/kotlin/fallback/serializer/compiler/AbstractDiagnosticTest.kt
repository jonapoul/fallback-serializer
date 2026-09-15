package fallback.serializer.compiler

import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_DEXING
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.DISABLE_GENERATED_FIR_TAGS
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.FULL_JDK
import org.jetbrains.kotlin.test.directives.TestPhaseDirectives.RUN_PIPELINE_TILL
import org.jetbrains.kotlin.test.runners.AbstractPhasedJvmDiagnosticLightTreeTest
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.TestPhase

// Compiles each file under src/test/data/diagnostic and checks the reported diagnostics against the
// inline markup and the neighbouring .diag.txt file
open class AbstractDiagnosticTest : AbstractPhasedJvmDiagnosticLightTreeTest() {
  override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider =
    EnvironmentBasedStandardLibrariesPathProvider

  override fun configure(builder: TestConfigurationBuilder): Unit =
    with(builder) {
      super.configure(builder)
      configurePlugin()

      defaultDirectives {
        +FULL_JDK
        +WITH_STDLIB
        +IGNORE_DEXING // Avoids loading R8 from the classpath
        +DISABLE_GENERATED_FIR_TAGS

        // Unless overridden, assume the test fails in the frontend
        RUN_PIPELINE_TILL.with(TestPhase.FRONTEND)
      }
    }
}
