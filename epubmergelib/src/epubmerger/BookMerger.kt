package epubmerger

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubWriter
import java.nio.file.Path

class BookMerger(var epubs: List<Book>) {
  val result = Book()
  val resources = HashMap<Pair<Int, String>, EpubResource>()

  fun mergeBooks() {
    addAllResources()

    epubs.forEachIndexed { index, epub ->
      if (epub.tableOfContents != null && epub.tableOfContents.size() > 0) {
        processTOC(epub, index)
      } else {
        // If there's no TOC in the book we have to just take
        // items from spine and process one after another
        epub.spine.spineReferences.forEach { spineReference ->
          // TODO do something when there's no TOC
        }
      }
    }

    result.generateSpineFromTableOfContents()
    generateMetadata()
  }

  internal fun processTOC(book: Book, index: Int) {
    val firstPageResource = if (book.coverPage != null) {
      book.coverPage
    } else {
      book.spine.getResource(0)
    }

    val topLevelTocReference = result.addSection(book.title, firstPageResource)
    book.tableOfContents.tocReferences.forEach { tocReference ->
      processTOCReference(index, tocReference, book, topLevelTocReference)
    }
  }

  private fun addAllResources() {
    epubs.forEachIndexed { index, epub ->
      epub.resources.all.forEach { res ->
        val key = index.to(res.href)
        if (!resources.containsKey(key)) {
          val epubResource = ResourceProcessor.createEpubResource(res.href, res.id, index)
          resources.put(key, epubResource)
          val res = Resource(
              epubResource.newId,
              ResourceProcessor.reprocessXhtmlFile(res.data, epub, index),
              epubResource.newHref,
              res.mediaType)
          result.resources.add(res)
        }
      }
    }
  }

  private fun processTOCReference(bookIndex: Int, sourceTocReference: TOCReference, book: Book, parent: TOCReference?) {
    // val href = "${bookIndex}_${sourceTocReference.resource.href}"
    val epubResource = ResourceProcessor.createEpubResource(
        sourceTocReference.resource.href,
        sourceTocReference.resourceId,
        bookIndex)

    // If below is untrue, then something disasterous happened and we cannot continue
    assert(result.resources.containsByHref(epubResource.newHref))

    val resource = result.resources.getByHref(epubResource.newHref)
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

  private fun generateMetadata() {
    val epr = EpubProcessor(emptyList())
    epr.book = result
    epr.generateMetadata(epubs)
  }

  fun writeBook(path: Path) {
    EpubWriter().write(result, path.toFile().outputStream())
  }
}