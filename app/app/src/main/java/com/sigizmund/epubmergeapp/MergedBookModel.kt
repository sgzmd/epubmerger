package com.sigizmund.epubmergeapp

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import java.nio.file.Paths

interface ReadOnlyModel {
  var books: List<Book>
  var bookTitle: String
  var bookAuthor: String

  var bookEntries: List<BookEntry>
}

class MergedBookModel(var sourceFiles: List<String>) : ReadOnlyModel {
  private var _books: List<Book> = sourceFiles.map { EpubReader().readEpub(Paths.get(it).toFile().inputStream()) }
  private var _bookTitle: String = _books.map { it.title }.joinToString { ", " }

  // Below code is, essentially, a dirty hack to get _some_ author for the merged book.
  // Largely this is not problem since it will be most likely edited by the user.
  private var _bookAuthor: String = _books
    .asSequence()
    .map { it.metadata.authors }
    .flatten()
    .map { "${it.firstname} ${it.lastname}" }
    .toSet()
    .joinToString { ", " }

  override var bookTitle: String
  get() {
    return _bookTitle
  }
  set(value) {
    _bookTitle = value
  }

  override var bookAuthor: String
  get() {
    return _bookAuthor
  }
  set(value) {
    _bookAuthor = value
  }

  override var books: List<Book> = _books
  get() { return _books }

  /**
   * When books order has changed, this method will be called.
   */
  private fun updateBooksOrder(entries: List<BookEntry>) {
    _books = entries.map { it.book }
    sourceFiles = entries.map { it.fileName }
  }

  override var bookEntries: List<BookEntry>
    get() = _books.mapIndexed { index, book ->
      BookEntry(book, sourceFiles[index])
    }

    set(value) {
      updateBooksOrder(value)
    }
}