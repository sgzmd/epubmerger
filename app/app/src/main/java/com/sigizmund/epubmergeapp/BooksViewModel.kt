package com.sigizmund.epubmergeapp

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nl.siegmann.epublib.epub.EpubReader
import org.jetbrains.anko.doAsync
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
    private var hasBeenModified = false
    override fun setValue(value: T) {
      hasBeenModified = true
      super.setValue(value)
    }

    override fun postValue(value: T) {
      hasBeenModified = true
      super.postValue(value)
    }
  }

  private var internalBookEntries: MutableLiveData<List<BookEntry>>? = null
  private var _bookTitle: MediatorLiveData<String>? = null
  private var _bookAuthor: MediatorLiveData<String>? = null

  var bookEntries: MutableLiveData<List<BookEntry>>? = null
    get() {
      if (internalBookEntries == null) {
        internalBookEntries = MutableLiveData()
        doAsync {
          val entries = sourceFiles.map { createBookEntry(it) }
          internalBookEntries?.postValue(entries)
        }
      }

      return internalBookEntries
    }

  var bookTitle: MutableLiveData<String>
    get() {
      if (_bookTitle == null) {
        _bookTitle = MediatorLiveData()
        updateDefaultTitle()
      }

      return _bookTitle!!
    }
    private set(value) {}

  var bookAuthor: MutableLiveData<String>
    get() {
      if (_bookAuthor == null) {
        _bookAuthor = MediatorLiveData()
        updateDefaultAuthor()
      }

      return _bookAuthor!!
    }
    private set(value) {}


  private fun updateDefaultTitle() {
    val titles = bookEntries?.value?.map { it.title }
    _bookTitle?.postValue(
      if (titles == null || titles.all { it.isBlank() }) {
        "Sample Title"
      } else {
        titles.filter { !it.isBlank() }.joinToString(", ")
      }
    )

    Log.d(TAG, "Book title: $_bookTitle")
  }

  private fun updateDefaultAuthor() {
    var authors =
      bookEntries?.value?.map { book: BookEntry ->
        book.book.metadata.authors
      }
        ?.flatten()
        ?.map { "${it.firstname} ${it.lastname}".trim() }
        ?.toSortedSet()

    _bookAuthor?.postValue(
      if (authors == null || authors.all { it.isBlank() }) {
        "Sample Author"
      } else {
        authors.filter { !it.isBlank() }.joinToString(", ")
      }
    )


    Log.d(TAG, "Book Author: $_bookAuthor")
  }
}

