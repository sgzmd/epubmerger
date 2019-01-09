package epubmerger

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.MediaType
import org.jsoup.Jsoup
import java.net.URI
import java.nio.file.Paths

object ResourceProcessor {

  class FullHref(fullHref: String) {
    val uri: URI = URI(fullHref)
    val href: String
      get() = uri.path

    val fragment: String?
      get() = uri.fragment

    fun withUpdatedHref(newHref: String): String {
      return if (fragment == null) {
        newHref
      } else {
        "$newHref#$fragment"
      }
    }

  }

  private val SUPPORTED_SRC_ATTRS = listOf("src", "href")

  /**
   * Updates XHTML file so that:
   * <ul>
   *   <li>It is now a well-formed XHTML</li>
   *   <li>All link/a/img tags in the document are up-to-date with the new resource names</li>
   * </ul>
   *
   * @param data an XHTML file in UTF-8, potentially non-well-formed. See also: Resource#getData()
   * @param book a Book object from which resource above was taken
   * @param index An index of the book in the final result
   *
   * @return a ByteArray with reprocessed valid XHTML
   */
  fun reprocessXhtmlFile(data: ByteArray, book: Book, index: Int): ByteArray {
    val soup = Jsoup.parse(String(data))
    val elements = soup.select("*")

    // Iterating over every single element in (X)HTML file
    elements.forEach {

      // If element has one of the supported src/href/whatever attributes
      for (attr in SUPPORTED_SRC_ATTRS) {
        if (!it.hasAttr(attr)) {
          continue
        }

        val href = FullHref(it.attr(attr))
        if (!book.resources.containsByHref(href.href)) {
          continue
        }

        // Constructing new EpubResource descriptor
        val res = book.resources.getByHref(href.href)
        val newEpubResource = createResourceName(res.href, res.id, index)
        it.attr(attr, href.withUpdatedHref(newEpubResource.newHref!!))
      }
    }

    return soup.toString().toByteArray()
  }

  fun createResourceName(href: String, id: String, bookIndex: Int): EpubResource {
    val path = Paths.get(href)
    val newhref = if (path.parent != null)
      "${path.parent}/${bookIndex}_${path.fileName}"
    else
      "${bookIndex}_${path.fileName}"

    return EpubResource(href, bookIndex, newhref, "${bookIndex}_${id}")
  }


}

private val MediaType.isTextBasedFormat: Boolean
  get() {
    return this.name in setOf("text/html", "application/xhtml+xml", "text/plain", "text/xml", "application/xml")
  }
