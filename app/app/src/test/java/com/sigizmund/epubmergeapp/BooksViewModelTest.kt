package com.sigizmund.epubmergeapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
    override fun createBookEntry(it: String): BookEntry {
      val mockEntry = Mockito.mock(BookEntry::class.java)
      `when`(mockEntry.author).thenReturn(listOf("Test Author1", "Test Author 2"))
      `when`(mockEntry.title).thenReturn("Test Title")
      `when`(mockEntry.fileName).thenReturn(it)
      `when`(mockEntry.book).thenReturn((mock(Book::class.java)))

      return mockEntry
    }
  }

  lateinit var booksViewModel: BooksViewModel

  @Before
  fun setUp() {
    booksViewModel = TestableBooksViewModel(listOf("file1.epub", "file2.epub"))
    /* val entries = */ booksViewModel.bookEntries
  }

  @Test
  fun smokeTest() {
    assertEquals("Test Title", booksViewModel.bookTitle.value)
  }
}