package com.sigizmund.epubmergeapp

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.util.*


private const val ARG_TITLE = "book_meta_title"
private const val ARG_AUTHOR = "book_meta_author"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BookMetaFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BookMetaFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class BookMetaFragment : Fragment() {
  // TODO: Rename and change types of parameters
  private val TAG = "BookMetaFragment"
  private var title: String? = null
  private var author: String? = null
  private var listener: OnFragmentInteractionListener? = null
  private lateinit var bookAuthor: EditText
  private lateinit var bookTitle: EditText

  private lateinit var model: BooksViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      var entries = it.getStringArrayList(SELECTED_FILES)
      model = ViewModelProviders.of(
        requireActivity(),
        BooksViewModel.BooksViewModelFactory(entries)
      )[BooksViewModel::class.java]
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_book_meta, container, false)

    bookAuthor = view.findViewById(R.id.bookAuthor)
    bookTitle = view.findViewById(R.id.bookTitle)

    bookAuthor.setText(model.bookAuthor)
    bookTitle.setText(model.bookTitle)

    model.bookEntries?.observe(this, Observer {
      Log.d(TAG, "BookAuthor changed")
      bookAuthor.setText(model.bookAuthor)
      bookTitle.setText(model.bookTitle)
    })



    bookAuthor.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(s: Editable?) {
        Log.d(TAG, "Book author has changed to ${s.toString()}, updating")

        // This has side effect: now title is considered to be "set", so it won't
        // be automatically updated when books are re-ordered
        model.bookAuthor = s.toString()
      }
    })

    bookTitle.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
      override fun afterTextChanged(s: Editable?) {
        Log.d(TAG, "Book title was changed to ${bookTitle.text}, updating")

        model.bookTitle = s.toString()
      }
    })

    return view
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
    fun onMetadataUpdated(title: String, author: String)
  }

  companion object {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @param author Parameter 2.
     * @return A new instance of fragment BookMetaFragment.
     */
    // TODO: Rename and change types and number of parameters
    @JvmStatic
    fun newInstance(model: ArrayList<String>) =
      BookMetaFragment().apply {
        arguments = Bundle().apply {
          putStringArrayList(SELECTED_FILES, model)
        }
      }
  }
}
