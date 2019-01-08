package epubmerger

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.TOCReference
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class BookMerger(var epubs: List<Book>) {
  val result = Book()

  fun mergeBooks() {
    epubs.forEachIndexed { index, epub ->
      if (epub.tableOfContents != null && epub.tableOfContents.size() > 0) {
        processTOC(epub, index)
      } else {
        // If there's no TOC in the book we have to just take
        // items from spine and process one after another
        epub.spine.spineReferences.forEach { spineReference ->

        }
      }
    }

    // and now go over all the resources which we may not have included and make
    // sure they are, in fact, included. Or should we do it on resource reprocessing
    // stage instead?
  }

  internal fun processTOC() {

  }

  internal fun processTOC(book: Book, index: Int) {
    book.tableOfContents.tocReferences.forEach { tocReference ->
      // TODO: use addSection and pass the result as parent

      if (book.coverPage != null) {

      }

      processTOCReference(index, tocReference, book, null)
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

    sourceTocReference.children.forEach { childTocEntry ->
      processTOCReference(bookIndex, childTocEntry, book, resultTOCReference)
    }
  }

  private fun reprocessResource(data: ByteArray, bookIndex: Int) {
    val soup = Jsoup.parse(String(data))
    val elements: Elements = soup.select("*")

  }
}