package epubmerger

import com.adobe.epubcheck.api.EpubCheck
import com.adobe.epubcheck.util.DefaultReportImpl
import com.google.common.truth.Truth.assertThat
import nl.siegmann.epublib.domain.Author
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
    tempFile.deleteOnExit()
    LOG.info(tempFile.absolutePath)

    epubMerger.writeBook(Paths.get(tempFile.absolutePath))

    val book = EpubReader().readEpub(tempFile.inputStream())
    assertThat(book).isNotNull()

    var expectedResourceIds = listOf(
        "ncx",
        "id_0_Section0002",
        "id_0_Section0003",
        "id_1_Section0002",
        "id_1_Section0003",
        "item_1",
        "item_2"
    )

    val expectedHrefs = listOf("toc.ncx",
        "Text/0_Section0002.xhtml",
        "Text/0_Section0003.xhtml",
        "Text/1_Section0002.xhtml",
        "Text/1_Section0003.xhtml",
        "Text/0_Section0001.xhtml",
        "Text/1_Section0001.xhtml"
    )

    assertThat(book.resources.all.map { it.id }).containsExactlyElementsIn(expectedResourceIds)
    assertThat(book.resources.all.map { it.href }).containsExactlyElementsIn(expectedHrefs)

    assertThat(book.metadata.titles).containsExactlyElementsIn(
        listOf(
            "Sample Book (образец) 1",
            "Sample Book (образец) 2",
            "Sample Book (образец) 1; Sample Book (образец) 2"))


    val report = DefaultReportImpl(tempFile.name)
    EpubCheck(tempFile, report).doValidate()

    // TODO: this must be 0 at some point
    assertThat(report.errorCount).isEqualTo(7)
  }

  @Test
  fun titleTest() {
    val files = listOf(
        Paths.get("testdata/sample1.epub"),
        Paths.get("testdata/sample2.epub"))
    val epubMerger = BookMerger(files.map { EpubReader().readEpub(it.toFile().inputStream()) })
    epubMerger.mergeBooks()

    epubMerger.mergedBookTitle = "MergedBookTitle"

    val tempFile = File.createTempFile("epubmerger-test", ".epub")
    tempFile.deleteOnExit()
    LOG.info(tempFile.absolutePath)

    epubMerger.writeBook(Paths.get(tempFile.absolutePath))

    val book = EpubReader().readEpub(tempFile.inputStream())
    assertThat(book).isNotNull()
    LOG.info("Book title is ${book.metadata.firstTitle}")
    assertThat(book.metadata.firstTitle).isEqualTo("MergedBookTitle")
  }

  @Test
  fun authorTest() {
    val files = listOf(
        Paths.get("testdata/sample1.epub"),
        Paths.get("testdata/sample2.epub"))
    val epubMerger = BookMerger(files.map { EpubReader().readEpub(it.toFile().inputStream()) })
    epubMerger.mergeBooks()

    epubMerger.mergedBookAuthor = "MyAuthor"

    val tempFile = File.createTempFile("epubmerger-test", ".epub")
    tempFile.deleteOnExit()
    LOG.info(tempFile.absolutePath)

    epubMerger.writeBook(Paths.get(tempFile.absolutePath))

    val book = EpubReader().readEpub(tempFile.inputStream())
    assertThat(book).isNotNull()
    LOG.info("Book title is ${book.metadata.firstTitle}")
    assertThat(book.metadata.authors).containsExactlyElementsIn(listOf(Author("MyAuthor")))
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