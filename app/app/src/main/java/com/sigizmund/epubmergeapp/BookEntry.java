package com.sigizmund.epubmergeapp;

import androidx.annotation.VisibleForTesting;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;

import java.util.ArrayList;
import java.util.List;

// For some reason, Mockito behaves strangely with Kotlin properties,
// so for simplicity of testing building this as a Java class.
public class BookEntry {
  private final String filePath;
  @VisibleForTesting final Book book;

  public BookEntry(Book book, String filePath) {
    this.filePath = filePath;
    this.book = book;
  }

  public String getTitle() {
    return book.getTitle();
  }

  public String getFileName() {
    return filePath;
  }

  public List<String> getAuthor() {
    List<String> result = new ArrayList<>();
    for (Author author : book.getMetadata().getAuthors()) {
      result.add(author.getFirstname() + " " + author.getLastname());
    }

    return result;
  }
}
