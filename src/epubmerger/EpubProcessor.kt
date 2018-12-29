package epubmerger

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubReader
import java.nio.file.Path

/**
 * Joins multiple EPub files into a single file.
 */
class EpubProcessor(files: List<Path>) {

  private var files: List<Path> = files
  private lateinit var book: Book
  private var epubIndex = 0

  fun mergeFiles() {
    val epubs = readFiles()
    book = Book()

    for (epub in epubs) {
      if (book.coverImage == null) {

      }
    }
  }

  internal fun reprocessAllResources(epub: Book) {
    epub.resources.all.forEach {

    }
  }

  internal fun buildCoverPage(epub: Book) {
    if (epub.coverImage != null && epub.coverPage != null) {
      book.coverImage = Resource(epub.coverImage.data, epub.coverImage.mediaType)

      val reprocessed = reprocessResource(epub.coverPage, epub.coverImage.href, book.coverImage.href)
      if (reprocessed != null) {
        book.coverPage = Resource(reprocessed, epub.coverPage.mediaType)
      }
    }
  }

  internal fun readFiles(): List<Book> {
    return this.files.map {
      EpubReader().readEpub(it.toFile().inputStream())
    }
  }
}