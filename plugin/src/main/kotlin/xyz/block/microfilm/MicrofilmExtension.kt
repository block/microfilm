package xyz.block.microfilm

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

abstract class MicrofilmExtension : CompressionSettings.Spec() {
  @get:Inject internal abstract val objects: ObjectFactory
  @get:Inject internal abstract val providers: ProviderFactory

  internal val compressionRules: Provider<List<CompressionRule>>
    get() = providers.provider {
      buildList {
        add(CompressionRule(pattern = "**", compressionSettings = resolve()))
        addAll(compressionRuleOverrides.get())
      }
    }

  internal abstract val compressionRuleOverrides: ListProperty<CompressionRule>

  /** Overrides the default compression settings for images matching the given glob [pattern]. */
  fun images(pattern: String, action: Action<CompressionSettings.Spec>) {
    val spec = objects.newInstance(CompressionSettings.Spec::class.java)
    action.execute(spec)
    compressionRuleOverrides.add(
      providers.provider {
        CompressionRule(pattern = pattern, compressionSettings = spec.resolve())
      }
    )
  }
}
