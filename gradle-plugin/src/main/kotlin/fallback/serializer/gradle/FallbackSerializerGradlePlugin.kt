package fallback.serializer.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

public class FallbackSerializerGradlePlugin : KotlinCompilerPluginSupportPlugin {
  override fun apply(target: Project) {
    // The compiler plugin uses internal compiler APIs, so it only works with the Kotlin version it
    // was built against
    target.plugins.withType(KotlinBasePlugin::class.java).configureEach { kotlin ->
      if (kotlin.pluginVersion != KOTLIN_VERSION) {
        throw GradleException(
          "Fallback Serializer $VERSION needs Kotlin $KOTLIN_VERSION, but ${target.displayName} uses " +
            "Kotlin ${kotlin.pluginVersion}"
        )
      }
    }

    // Without it, @Serializable(with = ...) isn't processed and the generated serializers aren't
    // used. A warning, in case the plugin is applied in a way this doesn't see.
    target.afterEvaluate {
      if (!target.pluginManager.hasPlugin(SERIALIZATION_PLUGIN_ID)) {
        target.logger.warn(
          "Fallback Serializer: ${target.displayName} doesn't apply $SERIALIZATION_PLUGIN_ID, so the " +
            "generated serializers won't be used"
        )
      }
    }
  }

  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

  override fun applyToCompilation(
    kotlinCompilation: KotlinCompilation<*>
  ): Provider<List<SubpluginOption>> {
    kotlinCompilation.defaultSourceSet.dependencies { implementation("$GROUP:runtime:$VERSION") }
    return kotlinCompilation.target.project.provider { emptyList() }
  }

  override fun getCompilerPluginId(): String = PLUGIN_ID

  override fun getPluginArtifact(): SubpluginArtifact =
    SubpluginArtifact(groupId = GROUP, artifactId = "compiler", version = VERSION)

  private companion object {
    const val SERIALIZATION_PLUGIN_ID = "org.jetbrains.kotlin.plugin.serialization"
  }
}
