package epubmerger

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.MediaType
import nl.siegmann.epublib.domain.Resource
import org.jsoup.Jsoup
import java.nio.file.Paths

object ResourceProcessor {
  private val SUPPORTED_SRC_ATTRS = listOf("src", "href")

  // TODO this will fail miserably if we have a circular refence, i.e. chapter 1 refers
  // TODO to footnotes page, and footnotes page refers back - rare but not inconceivable.
  // TODO We need to move away from recursion here, however nasty that sounds.
  fun reprocessXhtmlFile(data: ByteArray, book: Book, index: Int, result: Book): ByteArray {
    val soup = Jsoup.parse(String(data))
    val elements = soup.select("*")

    // Iterating over every single element in (X)HTML file
    elements.forEach {

      // If element has one of the supported src/href/whatever attributes
      for (attr in SUPPORTED_SRC_ATTRS) {
        if (!it.hasAttr(attr)) {
          continue
        }

        val href: String = it.attr(attr)
        if (!book.resources.containsByHref(href)) {
          continue
        }

        // Constructing new EpubResource descriptor
        val res = book.resources.getByHref(href)
        val newEpubResource = createResourceName(res.href, res.id, index)
        if (!result.resources.containsByHref(newEpubResource.newHref)) {
          // If newHref is not in the result's resources yet, let's reprocess it and add it -
          // it may or may not be referenced by TOC, better reprocess it here.
          var data: ByteArray? = null
          if (res.mediaType.isTextBasedFormat) {
            data = reprocessXhtmlFile(res.data, book, index, result)
          } else {
            data = res.data
          }

          // Adding resource, now we can change attr to it
          result.resources.add(Resource(newEpubResource.newId, data, newEpubResource.newHref, res.mediaType))
        }
      }
    }

    // TODO fix me
    return "".toByteArray()
  }

  fun createResourceName(href: String, id: String, bookIndex: Int): EpubResource {
    val path = Paths.get(href)
    val newhref = if (path.parent != null)
      "${path.parent}/${bookIndex}_${path.fileName}"
    else
      "${bookIndex}_${path.fileName}"

    val newid = "${bookIndex}_${id}"

    return EpubResource(href, bookIndex, newhref, newid)
  }
}

private val MediaType.isTextBasedFormat: Boolean
  get() {
    return this.name in setOf("text/html", "application/xhtml+xml", "text/plain", "text/xml", "application/xml")
  }
