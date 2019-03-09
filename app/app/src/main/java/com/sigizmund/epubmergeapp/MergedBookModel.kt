package com.sigizmund.epubmergeapp

import android.util.Log
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
  private val TAG = "MergedBookModel"

  private var _books: List<Book> = sourceFiles.map { EpubReader().readEpub(Paths.get(it).toFile().inputStream()) }
  private lateinit var _bookTitle: String
  private var _defaultTitleUsed = true

  private lateinit var _bookAuthor: String
  private var _defaultAuthorUsed = true

  init {
    updateDefaultTitle()
    updateDefaultAuthor()
  }

  private fun updateDefaultTitle() {
    val titles = _books.map { it.title }
    _bookTitle = if (titles.all { it.isBlank() }) {
      "Sample Title"
    } else {
      titles.filter { !it.isBlank() }.joinToString(", ")
    }

    Log.d(TAG, "Book title: $_bookTitle")
  }

  private fun updateDefaultAuthor() {
    var authors =
      _books.map { it.metadata.authors }.flatten().map { "${it.firstname} ${it.lastname}".trim() }.toSortedSet()
    _bookAuthor = if (authors.all { it.isBlank() }) {
      "Sample Author"
    } else {
      authors.filter { !it.isBlank() }.joinToString(", ")
    }

    Log.d(TAG, "Book Author: $_bookAuthor")
  }

  override var bookTitle: String
  get() {
    return _bookTitle
  }
  set(value) {
    _bookTitle = value
    _defaultTitleUsed = false
  }

  override var bookAuthor: String
  get() {
    return _bookAuthor
  }
  set(value) {
    _bookAuthor = value
    _defaultAuthorUsed = false
  }

  override var books: List<Book> = _books
  get() { return _books }

  /**
   * When books order has changed, this method will be called.
   */
  fun updateBooksOrder(entries: List<BookEntry>) {
    _books = entries.map { it.book }
    sourceFiles = entries.map { it.fileName }

    Log.d(TAG, "Books order has changed: ${_books.map { it.title }.joinToString(", ")}")

    if (_defaultTitleUsed) {
      updateDefaultTitle()
    }

    if (_defaultAuthorUsed) {
      updateDefaultAuthor()
    }
  }

  override var bookEntries: List<BookEntry>
    get() = _books.mapIndexed { index, book ->
      BookEntry(book, sourceFiles[index])
    }

    set(value) {
      updateBooksOrder(value)
    }
}