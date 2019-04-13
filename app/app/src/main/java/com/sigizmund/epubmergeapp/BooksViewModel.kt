package com.sigizmund.epubmergeapp

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import nl.siegmann.epublib.epub.EpubReader
import java.util.*

open class BooksViewModel(app: Application, var sourceFiles: ArrayList<Uri>) : AndroidViewModel(app) {
  private val TAG = "BooksViewModel"

  class BooksViewModelFactory(var sourceFiles: ArrayList<Uri>, val app: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      return BooksViewModel(app, sourceFiles) as T
    }
  }

  fun addSourceFiles(files: ArrayList<Uri>) {
    sourceFiles.addAll(files)
    internalBookEntries?.setValue(sourceFiles.map { createBookEntry(it) })
  }

  open fun createBookEntry(it: Uri): BookEntry {
    val istream = getApplication<Application>().contentResolver.openInputStream(it)
    return BookEntry(EpubReader().readEpub(istream), it)
  }

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
      if (_bookTitle == null ) {
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
      if (_bookAuthor == null ) {
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
      "Default"
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
      "Default"
      } else {
        authors.filter { !it.isBlank() }.joinToString(", ")
      }
  }
}

