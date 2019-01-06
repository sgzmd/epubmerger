package epubmerger

import com.google.common.truth.Truth.assertThat
import nl.siegmann.epublib.epub.EpubReader
import org.junit.Test
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths


class EpubMergeEndToEndTest {
  private val LOG = LoggerFactory.getLogger(EpubMergeEndToEndTest::class.java)

  @Test
  fun endToEndTest() {
    val files = listOf(
        Paths.get("testdata/sample1.epub"),
        Paths.get("testdata/sample2.epub"))
    val epubMerger = EpubProcessor(files)
    epubMerger.mergeFiles()

    val tempFile = File.createTempFile("epubmerger-test", ".epub")
    tempFile.deleteOnExit()

    epubMerger.writeBook(Paths.get(tempFile.absolutePath))

    val book = EpubReader().readEpub(tempFile.inputStream())
    assertThat(book).isNotNull()

    var expectedResources = HashMap<String, String>()
    for (i in 0..1) {
      for (j in 0..2) {
        expectedResources.put("id_${i}_$j", "href_${i}_${j}.xhtml")
      }
    }

    expectedResources.put("ncx", "toc.ncx")

    assertThat(book.resources.all.map { it.id }).containsExactlyElementsIn(expectedResources.keys)
    assertThat(book.resources.all.map { it.href }).containsExactlyElementsIn(expectedResources.values)

    assertThat(book.metadata.titles).containsExactlyElementsIn(
        listOf(
            "Sample Book (образец) 1",
            "Sample Book (образец) 2",
            "Sample Book (образец) 1; Sample Book (образец) 2"))
  }

  @Test
  fun mergeRealSamples() {
    val files = listOf(
        Paths.get("testdata/aliceDynamic.epub"),
        Paths.get("testdata/pg1342-images.epub"),
        Paths.get("testdata/pg2600-images.epub"))
    val epubMerger = EpubProcessor(files)
    epubMerger.mergeFiles()
    epubMerger.writeBook(Paths.get("test-result.epub"))
  }
}