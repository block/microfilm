package xyz.block.microfilm

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ProviderFactory
import xyz.block.microfilm.ImageSettings.Exclude

abstract class MicrofilmExtension {
  @get:Inject internal abstract val objects: ObjectFactory
  @get:Inject internal abstract val providers: ProviderFactory

  internal abstract val imageRules: ListProperty<ImageRule>

  /** Compresses the images matching the given glob [pattern]. Matches all images by default. */
  @JvmOverloads
  fun compress(pattern: String = "**", action: Action<ImageSettings.Compress.Spec>) {
    val spec = objects.newInstance(ImageSettings.Compress.Spec::class.java)
    action.execute(spec)
    imageRules.add(
      providers.provider { ImageRule(pattern = pattern, imageSettings = spec.resolve()) }
    )
  }

  /** Excludes the images matching the given glob [pattern]. */
  fun exclude(pattern: String) {
    imageRules.add(providers.provider { ImageRule(pattern = pattern, imageSettings = Exclude) })
  }
}
