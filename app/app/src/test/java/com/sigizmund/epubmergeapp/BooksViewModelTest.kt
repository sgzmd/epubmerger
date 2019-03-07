package com.sigizmund.epubmergeapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
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

  @Test
  fun testObserve() {
    booksViewModel.bookTitle.observeForever {
      assertEquals("MyBook", it)
    }


    booksViewModel.bookEntries?.postValue(listOf())
  }
}