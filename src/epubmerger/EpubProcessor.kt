package epubmerger

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubReader
import java.nio.charset.Charset
import java.nio.file.Path

/**
 * Joins multiple EPub files into a single file.
 */
class EpubProcessor(files: List<Path>) {

  private var files: List<Path> = files
  internal var book = Book()
  internal lateinit var hrefIdMap: Map<String, Pair<String, String>>

  fun mergeFiles() {
    val epubs = readFiles()

    val hrefIdMap = calculateResourceNames(epubs)
    for (epub in epubs) {
      if (epub.coverPage != null && epub.coverImage != null) {
        buildCoverPage(epub)
        break
      }
    }
  }

  internal fun calculateResourceNames(epubs: List<Book>): HashMap<String, Pair<String, String>> {
    // Old href to new href/id
    val map = HashMap<String, Pair<String, String>>()
    epubs.forEachIndexed { index, epub ->
      epub.resources.all.forEachIndexed { resIdx, resource ->
        val ext = getFileExtension(resource.href)
        val id = "id_${index}_${resIdx}"
        val href = "href_${index}_${resIdx}.$ext"

        map.put(resource.href, Pair(href, id))
      }
    }

    return map
  }

  internal fun reprocessResources(epubs: List<Book>) {
    epubs.forEach { epub ->
      epub.resources.all.forEach { res ->
        val hrefIdPair = hrefIdMap[res.href]
        val data: ByteArray = res.data
        book.addResource(Resource(
            hrefIdPair?.second,
            reprocessResourceData(res.data, res.mediaType.toString()),
            hrefIdPair?.first,
            res.mediaType))
      }
    }
  }

  internal fun reprocessResourceData(data: ByteArray, type: String): ByteArray {
    if (SUPPORTED_MEDIA_TYPES.contains(type)) {
      // This is a text type we understand, to a degree, OK to reprocess
      val str = String(data, Charset.forName("UTF-8"))

      // Very naive and non-efficient implementation for now
      var result = str
      for (oldHref in hrefIdMap.keys) {
        val quotedHref = "\"$oldHref\""
        val newQuotedHref = "\"${hrefIdMap[oldHref]?.first}\""
        while (result.indexOf(quotedHref) > 0) {
          result = result.replace(quotedHref, newQuotedHref)
        }
      }

      return result.toByteArray()
    } else {
      return data
    }
  }


  internal fun buildCoverPage(epub: Book) {
    assert(hrefIdMap != null, { "hrefIdMap must be initialised" })
    val newImageHrefId: Pair<String, String>? = hrefIdMap[epub.coverImage.href]
    val newCoverPageHrefId = hrefIdMap[epub.coverPage.href]

    book.coverImage = book.resources.getByHref(newImageHrefId?.first)
    book.coverPage = book.resources.getByHref(newCoverPageHrefId?.first)
  }

  internal fun readFiles(): List<Book> {
    return this.files.map {
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