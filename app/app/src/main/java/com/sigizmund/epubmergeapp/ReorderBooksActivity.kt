package com.sigizmund.epubmergeapp

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import epubmerger.EpubProcessor
import nl.siegmann.epublib.epub.EpubReader
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.slf4j.LoggerFactory
import java.nio.file.Paths

import kotlinx.android.synthetic.main.activity_reorder_books.*
import org.jetbrains.anko.activityUiThreadWithContext

class ReorderBooksActivity : AppCompatActivity() {
  private lateinit var adapter: BooksAdapter
  private lateinit var list: DragDropSwipeRecyclerView


  private val onItemDragListener = object : OnItemDragListener<BookEntry> {
    override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: BookEntry) {
      LOG.info("onItemDragged(initialPosition=$initialPosition finalPosition=$finalPosition item=${item}")
      val a = adapter.dataSet
      LOG.info(a.toString())
    }

    override fun onItemDragged(previousPosition: Int, newPosition: Int, item: BookEntry) {
      LOG.info("onItemDragged(previousPosition=$previousPosition newPosition=$newPosition item=${item}")
    }
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_reorder_books)
    list = findViewById(R.id.book_list)

    val fileNames = intent.extras?.getStringArrayList(SELECTED_FILES_KEY)
    doAsync {
      val bookEntries = fileNames
        ?.map { Paths.get(it).toFile().inputStream() }
        ?.map { EpubReader().readEpub(it) }
        ?.map { BookEntry(it) }

      adapter = BooksAdapter(bookEntries!!)
      uiThread {
        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(it)
        list.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
        list.dragListener = onItemDragListener
      }
    }

    merge_files_button.setOnClickListener {
      doAsync {
        val processor = EpubProcessor(emptyList())
        processor.mergeBooks(adapter.dataSet.map { it.book })
        val title = processor.book.title
        val fileName = title.replace(' ', '_').replace('/', '_')

        val downloads = Paths.get(Environment.getExternalStorageDirectory().absolutePath, "Download")
        val resultPath = Paths.get(downloads.toString(), "$fileName.epub")
        processor.writeBook(resultPath)

        activityUiThreadWithContext {        processor.mergeFiles()
          Toast.makeText(this, "Merged file saved to $resultPath", Toast.LENGTH_LONG).show()
        }
      }
    }
  }

  companion object {
    val SELECTED_FILES_KEY = "selected_files"
    private val LOG = LoggerFactory.getLogger(ReorderBooksActivity.javaClass)
  }
}
