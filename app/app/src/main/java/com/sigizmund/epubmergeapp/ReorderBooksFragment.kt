package com.sigizmund.epubmergeapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import kotlinx.android.synthetic.main.fragment_reorder_books.*
import nl.siegmann.epublib.epub.EpubReader
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.nio.file.Paths
import java.util.*


const val SELECTED_FILES = "selected_files_key"

class ReorderBooksFragment : Fragment() {
  private val TAG = ReorderBooksFragment::class.java.name
  private var listener: OnFragmentInteractionListener? = null
  private lateinit var selectedFiles: ArrayList<String>
  private lateinit var adapter: BooksAdapter


  private val onItemDragListener = object : OnItemDragListener<BookEntry> {
    override fun onItemDropped(initialPosition: Int, finalPosition: Int, item: BookEntry) {
      Log.d(TAG, "onItemDragged(initialPosition=$initialPosition finalPosition=$finalPosition item=${item}")
      val a = adapter.dataSet
      Log.d(TAG, a.toString())


    }

    override fun onItemDragged(previousPosition: Int, newPosition: Int, item: BookEntry) {
      Log.d(TAG, "onItemDragged(previousPosition=$previousPosition newPosition=$newPosition item=${item}")
    }
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let { extras ->
      // SELECTED_FILES must be passed into this fragment
      val entries = extras.getParcelableArrayList<BookEntry>(SELECTED_FILES)
      adapter = BooksAdapter(entries)
      // selectedFiles = extras.getStringArrayList(SELECTED_FILES)!!
      bookList.adapter = adapter
      bookList.layoutManager = LinearLayoutManager(context)
      bookList.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
      bookList.dragListener = onItemDragListener
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_reorder_books, container, false)
  }

  // TODO: Rename method, update argument and hook method into UI event
  fun onButtonPressed(uri: Uri) {
    listener?.onFragmentInteraction(uri)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnFragmentInteractionListener) {
      listener = context
    } else {
      throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
    }
  }

  override fun onDetach() {
    super.onDetach()
    listener = null
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   *
   *
   * See the Android Training lesson [Communicating with Other Fragments]
   * (http://developer.android.com/training/basics/fragments/communicating.html)
   * for more information.
   */
  interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    fun onFragmentInteraction(uri: Uri)
  }

  companion object {
    @JvmStatic
    fun newInstance(model: ReadOnlyModel) =
      ReorderBooksFragment().apply {
        arguments = Bundle().apply {
          putParcelableArrayList(SELECTED_FILES, ArrayList(model.bookEntries))
        }
      }
  }
}
