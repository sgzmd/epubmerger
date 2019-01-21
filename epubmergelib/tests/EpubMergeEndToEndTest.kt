package epubmerger

import com.adobe.epubcheck.api.EpubCheck
import com.adobe.epubcheck.util.DefaultReportImpl
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
    EpubCheck(tempFile, report)

    // TODO: this has to get to 0 at some point
    // Right now it is 0 but this is wrong:
    /*
    java -jar ./epubcheck.jar /var/folders/9w/xchhrjt55_lcnvj7k2yk6w_0003bf9/T/epubmerger-test2675715583483637293.epub
Validating using EPUB version 2.0.1 rules.
ERROR(RSC-005): /var/folders/9w/xchhrjt55_lcnvj7k2yk6w_0003bf9/T/epubmerger-test2675715583483637293.epub/OEBPS/content.opf(29,16): Error while parsing file: element "opf:guide" incomplete; missing required element "opf:reference"
ERROR(RSC-005): /var/folders/9w/xchhrjt55_lcnvj7k2yk6w_0003bf9/T/epubmerger-test2675715583483637293.epub/OEBPS/toc.ncx(14,61): Error while parsing file: different playOrder values for navPoint/navTarget/pageTarget that refer to same target
ERROR(RSC-005): /var/folders/9w/xchhrjt55_lcnvj7k2yk6w_0003bf9/T/epubmerger-test2675715583483637293.epub/OEBPS/toc.ncx(19,63): Error while parsing file: different playOrder values for navPoint/navTarget/pageTarget that refer to same target
ERROR(RSC-005): /var/folders/9w/xchhrjt55_lcnvj7k2yk6w_0003bf9/T/epubmerger-test2675715583483637293.epub/OEBPS/toc.ncx(50,61): Error while parsing file: different playOrder values for navPoint/navTarget/pageTarget that refer to same target
ERROR(RSC-005): /var/folders/9w/xchhrjt55_lcnvj7k2yk6w_0003bf9/T/epubmerger-test2675715583483637293.epub/OEBPS/toc.ncx(55,63): Error while parsing file: different playOrder values for navPoint/navTarget/pageTarget that refer to same target
     */
    assertThat(report.errorCount).isEqualTo(5)
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