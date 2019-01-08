package epubmerger

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.TOCReference

class ResourceManager(var epubs: List<Book>) {
  data class TocResource(val resourceHref: String, val resourceId: String, val fragment: String?, val title: String)

  val result = Book()

  internal fun calculateResourceNames() {

  }

  internal fun calculateResourceNames(book: Book, index: Int) {
    book.tableOfContents.tocReferences.forEach {
      processTOCReference(index, it, book)
    }
  }

  private fun processTOCReference(bookIndex: Int, sourceTocReference: TOCReference, book: Book) {
    val href = "${bookIndex}_${sourceTocReference.resource.href}"
    if (!result.resources.containsByHref(href)) {
      val id = "${bookIndex}_${sourceTocReference.resourceId}"
      // TODO: reprocess resource data
      val resource = Resource(id, sourceTocReference.resource.data, href, sourceTocReference.resource.mediaType)
      book.resources.add(resource)
    }

    val resource = result.resources.getByHref(href)
    val tocReference = if (sourceTocReference.fragmentId == null)
      TOCReference(sourceTocReference.title, resource)
    else
      TOCReference(sourceTocReference.title, resource, sourceTocReference.fragmentId)

    result.tableOfContents.addTOCReference(tocReference)
  }
}