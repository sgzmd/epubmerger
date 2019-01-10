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
    val epubMerger = BookMerger(files.map { EpubReader().readEpub(it.toFile().inputStream()) })
    epubMerger.mergeBooks()

    val tempFile = File.createTempFile("epubmerger-test", ".epub")
    // tempFile.deleteOnExit()
    LOG.info(tempFile.absolutePath)

    epubMerger.writeBook(Paths.get(tempFile.absolutePath))

    val book = EpubReader().readEpub(tempFile.inputStream())
    assertThat(book).isNotNull()

    var expectedResources = listOf(
        "item_0_Section0003.xhtml",
        "item_1_Section0001.xhtml",
        "item_0_Section0001.xhtml",
        "ncx",
        "item_1_Section0003.xhtml",
        "item_1_Section0002.xhtml",
        "item_1",
        "item_0_Section0002.xhtml")


    assertThat(book.resources.all.map { it.id }).containsExactlyElementsIn(expectedResources)
    assertThat(book.resources.all.map { it.href }).containsExactlyElementsIn(expectedResources)

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
    val epubMerger = BookMerger(files.map { EpubReader().readEpub(it.toFile().inputStream()) })
    epubMerger.mergeBooks()
    epubMerger.writeBook(Paths.get("test-result.epub"))
  }
}