package com.sigizmund.epubmergeapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import nl.siegmann.epublib.domain.Book
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock


class BooksViewModelTest {
  @Rule
  @JvmField
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  class TestableBooksViewModel(sourceFiles: List<String>) : BooksViewModel(sourceFiles) {
    override fun createBookEntry(filePath: String): BookEntry {
      val entry = mock<BookEntry> {
        on { author } doReturn listOf("Test Author1", "Test Author 2")
        on { title }  doReturn "Test Title " + filePath
        on { fileName } doReturn filePath
      }

      return entry
    }
  }

  lateinit var booksViewModel: BooksViewModel

  @Before
  fun setUp() {
    booksViewModel = TestableBooksViewModel(listOf("file1.epub", "file2.epub"))
  }

  @Test
  fun smokeTest() {
    assertEquals(
      "Test Title file1.epub, Test Title file2.epub",
      booksViewModel.bookTitle.value)
  }
}