package com.sigizmund.epubmergeapp

import android.os.Parcel
import android.os.Parcelable
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import java.nio.file.Paths

@Mockable
data class BookEntry(val book: Book, val fileName: String) : Parcelable {
  override fun writeToParcel(dest: Parcel?, flags: Int) {
    dest?.writeString(fileName)
  }

  override fun describeContents(): Int {
    return 0
  }

  val title: String
    get() = book.title

  val author: List<String>
    get() = book.metadata.authors.map { "${it.firstname} ${it.lastname}" }

  companion object CREATOR : Parcelable.Creator<BookEntry> {
    override fun createFromParcel(parcel: Parcel): BookEntry {
      val fileName = parcel.readString()
      return BookEntry(EpubReader().readEpub(Paths.get(fileName).toFile().inputStream()), fileName)
    }

    override fun newArray(size: Int): Array<BookEntry?> {
      return arrayOfNulls(size)
    }
  }
}