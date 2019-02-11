package com.sigizmund.epubmergeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_metadata.*

val BOOK_METADATA_TITLE = "book_metadata_title"
val BOOK_METADATA_AUTHOR = "book_metadata_author"

class MetadataActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_metadata)

    val title = intent?.getStringExtra(BOOK_METADATA_TITLE)
    val author = intent?.getStringExtra(BOOK_METADATA_AUTHOR)

    bookAuthor.setText(author)
    bookTitle.setText(title)
  }
}
