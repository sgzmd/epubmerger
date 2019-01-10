package epubmerger

import com.google.common.truth.Truth.assertThat
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import org.junit.Test
import java.nio.file.Paths

class ResourceProcessorTest {
  @Test
  fun testNewHrefGenerator() {
    val imagePngResource = ResourceProcessor.createEpubResource("image.png", "id1", 1)
    assertThat(imagePngResource.newHref).isEqualTo("1_image.png")
    assertThat(imagePngResource.newId).isEqualTo("1_id1")
    val imagesImagePng = ResourceProcessor.createEpubResource("images/image.png", "id1", 2)
    assertThat(imagesImagePng.newHref).isEqualTo("images/2_image.png")
    val veryLongPathImagePng = ResourceProcessor.createEpubResource(
        "very/long/path/image.png", "1", 1)
    assertThat(veryLongPathImagePng.newHref).isEqualTo("very/long/path/1_image.png")
  }

  @Test
  fun testFullHref() {
    val noFragmentHref = "images/image.png"
    val noFragment = ResourceProcessor.FullHref(noFragmentHref)
    assertThat(noFragment.href).isEqualTo("images/image.png")
    assertThat(noFragment.fragment).isNull()

    val withFragmentHref = "chapters/chapter.xhtml#footnote"
    val withFragment = ResourceProcessor.FullHref(withFragmentHref)
    assertThat(withFragment.href).isEqualTo("chapters/chapter.xhtml")
    assertThat(withFragment.fragment).isEqualTo("footnote")
  }

  @Test
  fun testReprocess() {
    val fis = Paths.get("./testdata/chekov.epub").toFile().inputStream()
    val book = EpubReader().readEpub(fis)
    val data = book.resources.getByHref("ch1.xhtml").data
    val newData = ResourceProcessor.reprocessXhtmlFile(data, book, 1)


    val soup = Jsoup.parse(String(newData))
    val a = soup.select("a")[0]
    assertThat(a.attr("href")).isEqualTo("1_ch2.xhtml#id1")

    val link = soup.select("link")[0]
    assertThat(link.attr("href")).isEqualTo("1_style.css")
  }
}

