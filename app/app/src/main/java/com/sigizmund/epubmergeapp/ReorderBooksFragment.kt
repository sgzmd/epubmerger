package com.sigizmund.epubmergeapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import java.util.*


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
      val entries = extras.getStringArrayList(SELECTED_FILES)
      model = ViewModelProviders.of(
        requireActivity(),
        BooksViewModel.BooksViewModelFactory(entries)
      )[BooksViewModel::class.java]

      adapter = BooksAdapter(ArrayList<BookEntry>())
      model.bookEntries?.observe(this, androidx.lifecycle.Observer { entries ->
        adapter.dataSet = entries
      })
    }
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
    fun newInstance(selectedFiles: ArrayList<String>) =
      ReorderBooksFragment().apply {
        arguments = Bundle().apply {
          putStringArrayList(SELECTED_FILES, selectedFiles)
        }
      }
  }
}
