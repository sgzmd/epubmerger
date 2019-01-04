package com.sigizmund.epubmergeapp

import nl.siegmann.epublib.domain.Book

data class BookEntry(val book: Book) {
  val title: String
    get() = book.title

  val author: List<String>
    get() = book.metadata.authors.map { "${it.firstname} ${it.lastname}" }
}