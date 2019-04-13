package com.sigizmund.epubmergeapp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sigizmund.epubmergeapp.BookMergeWizardActivity.Companion.FILE_SELECT_CODE
import kotlinx.android.synthetic.main.fragment_reorder_books.*
import java.util.*
import kotlin.collections.ArrayList


const val SELECTED_FILES = "selected_files_key"

class ReorderBooksFragment : Fragment() {
  private val TAG = ReorderBooksFragment::class.java.name
  private var listener: BooksReoderListener? = null

  private lateinit var adapter: BooksAdapter
  private lateinit var bookList: DragDropSwipeRecyclerView
  private lateinit var model: BooksViewModel


  private val onItemDragListener = object : OnItemDragListener<BookEntry> {
    override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: BookEntry) {
      Log.d(TAG, "onItemDragged(initialPosition=$initialPosition finalPosition=$finalPosition item=${item}")
      val a = adapter.dataSet
      Log.d(TAG, a.toString())

      listener?.onBooksOrderChanged(adapter.dataSet)

      model.bookEntries?.postValue(adapter.dataSet)
    }

    override fun onItemDragged(previousPosition: Int, newPosition: Int, item: BookEntry) {
      Log.d(TAG, "onItemDragged(previousPosition=$previousPosition newPosition=$newPosition item=${item}")
    }
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let { extras ->
      // SELECTED_FILES must be passed into this fragment
      val entries = extras.getParcelableArrayList<Uri>(SELECTED_FILES)
      model = ViewModelProviders.of(
        requireActivity(),
        BooksViewModel.BooksViewModelFactory(entries, activity?.application!!)
      )[BooksViewModel::class.java]

      adapter = BooksAdapter(ArrayList<BookEntry>())
      model.bookEntries?.observe(this, androidx.lifecycle.Observer { entries ->
        adapter.dataSet = entries
      })

      model.bookEntries?.observe(this, androidx.lifecycle.Observer {
        adapter.dataSet = it
      })
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      FILE_SELECT_CODE -> {
        if (data != null) {
          val fileList = ArrayList<Uri>()
          if (data.clipData != null) {
            val clipData = data.clipData
            for (i in 0 until clipData.itemCount) {
              fileList.add(clipData.getItemAt(i).uri)
            }
          } else if (data.data != null) {
            fileList.add(data.data)
          }
          model.addSourceFiles(fileList)
        }
      }
    }
    super.onActivityResult(requestCode, resultCode, data)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    val view = inflater.inflate(R.layout.fragment_reorder_books, container, false)
    bookList = view.findViewById(R.id.bookList)

    bookList.adapter = adapter
    bookList.layoutManager = LinearLayoutManager(context)
    bookList.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
    bookList.dragListener = onItemDragListener
    bookList.swipeListener = object: OnItemSwipeListener<BookEntry> {
      override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: BookEntry) {
        val entries = model.bookEntries?.value!!
        AlertDialog.Builder(this@ReorderBooksFragment.context!!)
          .setTitle("Remove book from the list?")
          .setMessage("Do you really want to remove ${item.title} from the list? This cannot be undone")
          .setIcon(android.R.drawable.ic_dialog_alert)
          .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
            val list = ArrayList<BookEntry>(entries)
            list.removeAt(position)
            model?.bookEntries?.value = list
          })
          .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
            model.bookEntries?.value = entries
          })
          .show()
      }
    }

    val addBook = view.findViewById<FloatingActionButton>(R.id.add_book)
    addBook.setOnClickListener {
      val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/epub+zip"
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        putExtra("android.content.extra.SHOW_ADVANCED", true)
      }

      startActivityForResult(intent, BookMergeWizardActivity.FILE_SELECT_CODE)
    }


    return view
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is BooksReoderListener) {
      listener = context
    } else {
      throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
    }
  }

  override fun onDetach() {
    super.onDetach()
    listener = null
  }


  interface BooksReoderListener {
    fun onBooksOrderChanged(entries: List<BookEntry>)
  }

  companion object {
    @JvmStatic
    fun newInstance(selectedFiles: ArrayList<Uri>) =
      ReorderBooksFragment().apply {
        arguments = Bundle().apply {
          putParcelableArrayList(SELECTED_FILES, selectedFiles)
        }
      }
  }
}
