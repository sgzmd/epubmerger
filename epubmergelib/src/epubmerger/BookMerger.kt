package epubmerger

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.TOCReference

class BookMerger(var epubs: List<Book>) {
  val result = Book()

  internal fun calculateResourceNames() {

  }

  internal fun calculateResourceNames(book: Book, index: Int) {
    book.tableOfContents.tocReferences.forEach {
      processTOCReference(index, it, book, null)
    }
  }

  private fun processTOCReference(bookIndex: Int, sourceTocReference: TOCReference, book: Book, parent: TOCReference?) {
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

    var resultTOCReference = if (parent == null)
      result.tableOfContents.addTOCReference(tocReference)
    else parent.addChildSection(tocReference)

    sourceTocReference.children.forEach {
      processTOCReference(bookIndex, it, book, resultTOCReference)
    }
  }
}