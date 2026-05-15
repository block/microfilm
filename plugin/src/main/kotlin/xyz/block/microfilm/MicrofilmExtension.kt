package xyz.block.microfilm

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ProviderFactory

abstract class MicrofilmExtension {
  @get:Inject internal abstract val objects: ObjectFactory
  @get:Inject internal abstract val providers: ProviderFactory

  internal abstract val compressionRules: ListProperty<CompressionRule>

  /** Compresses all images using the given settings. */
  fun compress(action: Action<CompressionSettings.Spec>) {
    compress(pattern = "**", action = action)
  }

  /** Compresses images matching the given glob [pattern] using the given settings. */
  fun compress(pattern: String, action: Action<CompressionSettings.Spec>) {
    val spec = objects.newInstance(CompressionSettings.Spec::class.java)
    action.execute(spec)
    compressionRules.add(
      providers.provider {
        CompressionRule(pattern = pattern, compressionSettings = spec.resolve())
      }
    )
  }
}
