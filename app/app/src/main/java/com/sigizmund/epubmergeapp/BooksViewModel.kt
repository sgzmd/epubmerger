package com.sigizmund.epubmergeapp

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
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

    data class UriAndFileName(val uri: Uri, val fileName: String?)
    val fileNames = files.map { UriAndFileName(it, uriToFileName(it)) }
    val uris = fileNames.sortedBy { it.fileName }.map { it.uri }

    sourceFiles.addAll(uris)
    internalBookEntries?.setValue(sourceFiles.map { createBookEntry(it) })
  }

  fun uriToFileName(uri: Uri) : String? {
    val contentResolver = getApplication<Application>().contentResolver
    val cursor: Cursor =contentResolver.query(
      uri, null, null, null, null)
    var fileName: String? = null
    try {
      if (cursor != null && cursor.moveToFirst()) {
        fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
      }
    } finally {
      cursor.close()
    }

    if (fileName != null) {
      return fileName
    }

    return null
  }

  open fun createBookEntry(it: Uri): BookEntry {
    val contentResolver = getApplication<Application>().contentResolver

    val istream = contentResolver.openInputStream(it)
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

