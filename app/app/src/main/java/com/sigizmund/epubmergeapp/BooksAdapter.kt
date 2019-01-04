package com.sigizmund.epubmergeapp

import android.view.View
import android.widget.TextView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter

/**
 * Provides an adapter for re-orderable book list.
 */
class BooksAdapter(dataSet: List<BookEntry> = emptyList())
  : DragDropSwipeAdapter<BookEntry, BooksAdapter.ViewHolder>(dataSet) {

  override fun getViewHolder(itemView: View): ViewHolder = BooksAdapter.ViewHolder(itemView)

  override fun getViewToTouchToStartDraggingItem(item: BookEntry, viewHolder: ViewHolder, position: Int): View? {
    return viewHolder.bookTitle
  }

  override fun onBindViewHolder(item: BookEntry, viewHolder: ViewHolder, position: Int) {
    viewHolder.bookTitle.text = item.title
    viewHolder.bookAuthor.text = item.author.joinToString()
  }

  class ViewHolder(itemView: View) : DragDropSwipeAdapter.ViewHolder(itemView) {
    val bookTitle: TextView  = itemView.findViewById(R.id.book_title)
    val bookAuthor: TextView = itemView.findViewById(R.id.book_author)
  }
}