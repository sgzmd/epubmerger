package com.sigizmund.epubmergeapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Metadata
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner.Silent::class)
class BooksViewModelTest {
  @Rule
  @JvmField
  val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

  companion object {
    fun createMockBookEntry(
      author1: String,
      author2: String,
      bookTitle: String,
      filePath: String
    ): BookEntry {

      val bookAuthors = listOf(Author(author1), Author(author2))
      val bookMetadata = mock<Metadata> {
        on { authors } doReturn bookAuthors
      }
      val mockBook = mock<Book> {
        on { metadata } doReturn bookMetadata
      }

      val entry = mock<BookEntry> {
        on { author } doReturn listOf(author1, author2)
        on { title } doReturn bookTitle + " " + filePath
        on { fileName } doReturn filePath
        on { getBook() } doReturn mockBook
      }

      return entry
    }
  }

  class TestableBooksViewModel(sourceFiles: List<String>) : BooksViewModel(sourceFiles) {
    override fun createBookEntry(filePath: String): BookEntry {
      return createMockBookEntry("Test Author 1", "Test Author 2", "Test Title", filePath)
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
      booksViewModel.bookTitle
    )
    assertEquals(
      "Test Author 1, Test Author 2", booksViewModel.bookAuthor
    )
  }

  @Test
  fun testObserve_Author() {
    booksViewModel.bookEntries?.postValue(
      listOf(
        createMockBookEntry(
          "Author1", "Author2", "MyTitle", "file.epub"
        )
      )
    )

    booksViewModel.bookEntries?.observeForever {
      assertEquals("MyTitle file.epub", booksViewModel.bookTitle)
    }
  }
}