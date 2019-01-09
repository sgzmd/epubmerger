package epubmerger

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResourceProcessorTest {
  @Test
  fun testNewHrefGenerator() {
    val imagePngResource = ResourceProcessor.createResourceName("image.png", "id1", 1)
    assertThat(imagePngResource.newHref).isEqualTo("1_image.png")
    assertThat(imagePngResource.newId).isEqualTo("1_id1")
    val imagesImagePng = ResourceProcessor.createResourceName("images/image.png", "id1", 2)
    assertThat(imagesImagePng.newHref).isEqualTo("images/2_image.png")
    val veryLongPathImagePng = ResourceProcessor.createResourceName(
        "very/long/path/image.png", "1", 1)
    assertThat(veryLongPathImagePng.newHref).isEqualTo("very/long/path/1_image.png")
  }
}