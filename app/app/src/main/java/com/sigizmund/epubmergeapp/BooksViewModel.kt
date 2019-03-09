package com.sigizmund.epubmergeapp

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nl.siegmann.epublib.epub.EpubReader
import java.nio.file.Paths

open class BooksViewModel(var sourceFiles: List<String>) : ViewModel() {
  private val TAG = "BooksViewModel"

  class BooksViewModelFactory(var sourceFiles: List<String>) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      return BooksViewModel(sourceFiles) as T
    }
  }

  open fun createBookEntry(it: String) =
    BookEntry(EpubReader().readEpub(Paths.get(it).toFile().inputStream()), it)

  class DefaultLiveData<T>(val defaultValue: T) : MediatorLiveData<T>() {
    var hasBeenModified = false

    override fun setValue(value: T) {
      hasBeenModified = true
      super.setValue(value)
    }

    override fun postValue(value: T) {
      hasBeenModified = true
      super.postValue(value)
    }
  }

  private var internalBookEntries: DefaultLiveData<List<BookEntry>>? = null
  private var _bookTitle: String? = null
  private var _bookAuthor: String? = null

  var bookEntries: MutableLiveData<List<BookEntry>>? = null
    get() {
      if (internalBookEntries == null || internalBookEntries?.hasBeenModified != true) {
        internalBookEntries = DefaultLiveData(listOf<BookEntry>())

        val entries = sourceFiles.map { createBookEntry(it) }
        internalBookEntries?.setValue(entries)
      }

      return internalBookEntries
    }

  var bookTitle: String
    get() {
      if (_bookTitle == null) {
        return defaultTitle()
      } else {
        return _bookTitle!!
      }
    }
    set(value) {
      _bookTitle = value
    }

  var bookAuthor: String
    get() {
      if (_bookAuthor == null) {
        return defaultAuthor()
      } else {
        return _bookAuthor!!
      }
    }
    set(value) {
      _bookAuthor = value
    }


  private fun defaultTitle(): String {
    val titles = bookEntries?.value?.map { it.title }
    return if (titles == null || titles.all { it.isBlank() }) {
      "Sample Title"
    } else {
      titles.filter { !it.isBlank() }.joinToString(", ")
    }
  }

  private fun defaultAuthor(): String {
    var authors =
      bookEntries?.value?.map { book: BookEntry ->
        book.getBook().metadata.authors
      }
        ?.flatten()
        ?.map { "${it.firstname} ${it.lastname}".trim() }
        ?.toSet()

    return if (authors == null || authors.all { it.isBlank() }) {
        "Sample Author"
      } else {
        authors.filter { !it.isBlank() }.joinToString(", ")
      }
  }
}

