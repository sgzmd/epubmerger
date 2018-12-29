package epubmerger

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.SpineReference
import nl.siegmann.epublib.epub.EpubReader
import nl.siegmann.epublib.epub.EpubWriter
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import java.nio.file.Path

/**
 * Joins multiple EPub files into a single file.
 */
class EpubProcessor(files: List<Path>) {

  class ResourceObject(href: String, idx: Int) {
    val originalHref = href
    val bookIndex = idx
    var newHref : String? = null
    var newId : String? = null

    fun key(): String {
      return makeKey(bookIndex, originalHref)
    }

    companion object {
      fun makeKey(idx: Int, href: String): String {
        return "${idx}_${href}"
      }
    }
  }

  private val LOG = LoggerFactory.getLogger(EpubProcessor.javaClass)

  private var files: List<Path> = files
  internal var book = Book()
  internal lateinit var hrefIdMap: Map<String, ResourceObject>

  fun mergeFiles() {
    val epubs = readFiles()

    hrefIdMap = calculateResourceNames(epubs)
    reprocessResources(epubs)

    var idx = 0
    for (epub in epubs) {
      if (epub.coverPage != null && epub.coverImage != null) {
        buildCoverPage(epub, idx)
        break
      } else {
        ++idx
      }
    }

    buildSpine(epubs)
  }

  fun writeBook(path: Path) {
    EpubWriter().write(book, path.toFile().outputStream())
  }

  internal fun calculateResourceNames(epubs: List<Book>): HashMap<String, ResourceObject> {
    // Old href to new href/id
    LOG.info("Calculating new resource names for ${epubs.size} books")
    val map = HashMap<String, ResourceObject>()
    epubs.forEachIndexed { index, epub ->
      epub.resources.all.forEachIndexed { resIdx, resource ->
        val ext = getFileExtension(resource.href)
        val id = "id_${index}_${resIdx}"
        val href = "href_${index}_${resIdx}.$ext"

        LOG.info("In ${epub.title} assigning ${resource.href} new href=$href and id=$id")

        // XXX: one can't just key this by href because it can duplicate across multiple books!
        val ro = ResourceObject(resource.href, index)
        ro.newHref = href
        ro.newId = id
        map.put(ro.key(), ro)
      }
    }

    return map
  }

  internal fun reprocessResources(epubs: List<Book>) {
    LOG.info("Reprocessing resources for ${epubs.size} books")
    epubs.forEachIndexed { index, epub ->
      epub.resources.all.forEach { res ->
        LOG.info("For book ${epub.title} reprocessing ${res.href}")

        val hrefIdPair = hrefIdMap[ResourceObject.makeKey(index, res.href)]
        LOG.debug("${res.href} => $hrefIdPair")
        val r = book.addResource(Resource(
            hrefIdPair?.newId,
            reprocessResourceData(res.data, res.mediaType.toString()),
            hrefIdPair?.newHref,
            res.mediaType))

        LOG.debug("New resource was added as id=${r.id} href=${r.href}")
      }
    }
  }

  internal fun buildSpine(epubs: List<Book>) {
    for (epub in epubs) {
      for (si in epub.spine.spineReferences) {
        val href = si.resource.href
        val newHref = hrefIdMap[href]?.newHref
        book.spine.addSpineReference(SpineReference(book.resources.getByHref(newHref)))
      }
    }
  }

  internal fun reprocessResourceData(data: ByteArray, type: String): ByteArray {
    if (SUPPORTED_MEDIA_TYPES.contains(type)) {
      // This is a text type we understand, to a degree, OK to reprocess
      val str = String(data, Charset.forName("UTF-8"))

      // Very naive and non-efficient implementation for now
      var result = str
      for (key in hrefIdMap.keys) {
        val res = hrefIdMap[key]
        val quotedHref = "\"${res?.originalHref}\""
        val newQuotedHref = "\"${hrefIdMap[key]?.newHref}\""
        while (result.indexOf(quotedHref) > 0) {
          result = result.replace(quotedHref, newQuotedHref)
        }
      }

      return result.toByteArray()
    } else {
      return data
    }
  }


  internal fun buildCoverPage(epub: Book, idx: Int) {
    assert(hrefIdMap != null, { "hrefIdMap must be initialised" })
    val newImageHrefId = hrefIdMap[ResourceObject.makeKey(idx, epub.coverImage.href)]
    val newCoverPageHrefId = hrefIdMap[ResourceObject.makeKey(idx, epub.coverPage.href)]

    book.coverImage = book.resources.getByHref(newImageHrefId?.newHref)
    book.coverPage = book.resources.getByHref(newCoverPageHrefId?.newHref)
  }

  internal fun readFiles(): List<Book> {
    return this.files.map {
      LOG.info("Loading file $it")
      EpubReader().readEpub(it.toFile().inputStream())
    }
  }

  companion object {
    internal fun getFileExtension(fileName: String): String {
      val dotIdx = fileName.indexOf('.')
      if (dotIdx >= 0) {
        return fileName.substring(dotIdx + 1)
      } else {
        return ""
      }
    }

    val SUPPORTED_MEDIA_TYPES = setOf("text/html", "application/xhtml+xml", "text/plain", "text/xml", "application/xml")
  }
}