package epubmerger

import com.google.common.truth.Truth
import nl.siegmann.epublib.epub.EpubReader
import org.junit.Test
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths

class NoTocEndToEnd {
  private val LOG = LoggerFactory.getLogger(NoTocEndToEnd::class.java)

  @Test
  fun noTocEndToEndTest() {
    val files = listOf(
        Paths.get("testdata/alice_no_toc.epub"),
        Paths.get("testdata/alice_no_toc.epub"))
    val epubMerger = BookMerger(files.map { EpubReader().readEpub(it.toFile().inputStream()) })
    epubMerger.mergeBooks()

    val tempFile = File.createTempFile("epubmerger-test", ".epub")
    tempFile.deleteOnExit()
    LOG.info(tempFile.absolutePath)

    epubMerger.writeBook(Paths.get(tempFile.absolutePath))

    val book = EpubReader().readEpub(tempFile.inputStream())
    Truth.assertThat(book).isNotNull()

    Truth.assertThat(book.tableOfContents).isNotNull()
    Truth.assertThat(book.tableOfContents.size()).isEqualTo(2)
  }
}
