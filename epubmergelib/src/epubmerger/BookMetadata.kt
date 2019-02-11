package epubmerger

import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book

data class BookMetadata(var author: String, var title: String)

object InitialMetadataProvider {
  fun computeInitialMetadata(books: List<Book>) : BookMetadata {
    val author = books.map { it.metadata.authors }.flatten<Author?>().distinct().joinToString(", ")
    val title = books.map { it.metadata.titles }.flatten<String?>().joinToString(", ")

    return BookMetadata(author, title)
  }
}