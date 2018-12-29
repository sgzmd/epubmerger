import epubmerger.EpubProcessor
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class EpubProcessorTest {

    lateinit var book: Book
    lateinit var sut: EpubProcessor

    @BeforeEach
    fun setUp() {
        val fis = Paths.get("./testdata/chekov.epub").toFile().inputStream()
        book = EpubReader().readEpub(fis)
        sut = EpubProcessor(emptyList())
    }

    @Test
    fun testReprocessResources() {
        val map = sut.calculateResourceNames(listOf(book))
        sut.hrefIdMap = map
        sut.reprocessResources(listOf(book))
        map.forEach { oldHref, hrefIdPair ->
            assertTrue(book.resources.containsByHref(oldHref))
            assertTrue(sut.book.resources.containsByHref(hrefIdPair.first))
            assertTrue(sut.book.resources.containsId(hrefIdPair.second))
        }
    }

    @Test
    fun testReprocessResourceData() {
        sut.hrefIdMap = sut.calculateResourceNames(listOf(book))

        // very small chapter, but contains reference to style.css
        val ch2 = book.resources.getByHref("OPS/ch2.xhtml")
        val result = sut.reprocessResourceData(ch2.data, "application/xhtml+xml")

//        The text should be in this form:
//        <....>
//        <link rel="stylesheet" href="href_0_7.css" type="text/css"/>
//        <....>
//
//        Although the name of the resource is not guaranteed

        val pattern = Pattern.compile(".+href=\"href_[0-9]+_[0-9]+\\.css\".+", Pattern.MULTILINE)
        assertTrue(pattern.matcher(String(result).replace('\n', ' ')).matches())

        assertNotEquals(result, ch2.data)
    }

    @Test
    fun testGetFileExtension_HasExtension() {
        assertEquals("name", EpubProcessor.getFileExtension("file.name"))
    }

    @Test
    fun testGetFileExtension_NoExtension() {
        assertEquals("", EpubProcessor.getFileExtension("filename"))
    }

    @Test
    fun testGetFileExtension_HiddenFile() {
        assertEquals("filename", EpubProcessor.getFileExtension(".filename"))
    }

}
