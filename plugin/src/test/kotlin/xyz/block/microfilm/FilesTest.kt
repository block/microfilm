package xyz.block.microfilm

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import java.io.File
import org.junit.jupiter.api.Test

class FilesTest {
  @Test
  fun `isPngDrawable for webp drawable`() {
    assertThat(File("src/main/drawable/image.webp").isPngDrawable).isFalse()
  }

  @Test
  fun `isPngDrawable for png asset`() {
    assertThat(File("src/main/asset/image.png").isPngDrawable).isFalse()
  }

  @Test
  fun `isPngDrawable for png drawables`() {
    assertThat(File("src/main/drawable/image.png").isPngDrawable).isTrue()
    assertThat(File("src/main/drawable-hdpi/image.png").isPngDrawable).isTrue()
  }

  @Test
  fun `isPngDrawable for png nine-patch`() {
    assertThat(File("src/main/drawable/image.9.png").isPngDrawable).isFalse()
  }
}
