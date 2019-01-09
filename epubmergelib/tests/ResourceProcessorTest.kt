package epubmerger

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResourceProcessorTest {
  @Test
  fun testNewHrefGenerator() {
    assertThat(ResourceProcessor.createResourceName(
        "image.png", 1)).isEqualTo("1_image.png")
    assertThat(ResourceProcessor.createResourceName(
        "images/image.png", 1)).isEqualTo("images/1_image.png")
    assertThat(ResourceProcessor.createResourceName(
        "very/long/path/image.png", 1)).isEqualTo("very/long/path/1_image.png")
  }
}