package com.sigizmund.epubmergeapp

import android.content.Intent

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import epubmerger.BookMerger
import kotlinx.android.synthetic.main.activity_book_merge_wizard.*
import kotlinx.android.synthetic.main.fragment_reorder_books.*
import java.text.Normalizer
import java.util.*

private const val NUM_PAGES = 2

class BookMergeWizardActivity :
  AppCompatActivity(),
  ReorderBooksFragment.BooksReoderListener,
  BookMetaFragment.OnFragmentInteractionListener {

  companion object {
    val FILE_SELECT_CODE = 44
  }

  private val TAG = "BookMergeWizardActivity"
  private val SAVE_FILE_ACTION = 43


  private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int = NUM_PAGES
    override fun getItem(position: Int): Fragment {
      Log.d(TAG, "getItem($position)")
      val item: Fragment = when (position) {
        0 -> {
          ReorderBooksFragment.newInstance(selectedFiles)
        }

        1 -> {
          BookMetaFragment.newInstance(selectedFiles)
        }

        else -> {
          throw RuntimeException("Unsupported index")
        }
      }

      return item
    }

  }

  lateinit var selectedFiles: ArrayList<Uri>
  lateinit var bookViewModel: BooksViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_book_merge_wizard)

    buttonNext.setOnClickListener {
      if (viewPager.currentItem == NUM_PAGES - 1) {
        finishWizard()
      } else {
        viewPager.currentItem++
      }
    }

    buttonPrevious.setOnClickListener {
      viewPager.currentItem--
    }

    viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrollStateChanged(state: Int) {
        // do nothing
      }

      override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // do nothing
      }

      override fun onPageSelected(position: Int) {
        Log.d(TAG, "onPageSelected($position)")
        updateButtonsState(position)
      }
    })

    viewPager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
    selectedFiles = intent?.extras?.getParcelableArrayList<Uri>(SELECTED_FILES) ?: arrayListOf<Uri>()

    // For initial page it should be always disabled since this is the first page
    buttonPrevious.isEnabled = false

    this.bookViewModel = ViewModelProviders.of(
      this,
      BooksViewModel.BooksViewModelFactory(selectedFiles, application)
    )[BooksViewModel::class.java]
  }

  override fun onBooksOrderChanged(entries: List<BookEntry>) {
    // model?.updateBooksOrder(entries)
    bookViewModel.bookEntries?.postValue(entries)
  }

  override fun onMetadataUpdated(title: String, author: String) {
    bookViewModel.bookTitle = title
    bookViewModel.bookAuthor = author
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      SAVE_FILE_ACTION -> {
        if (data != null) {
          val uri = data.data!!

          val merger = BookMerger(this.bookViewModel.bookEntries?.value?.map { it.book }!!)
          val title = bookViewModel.bookTitle

          merger.mergedBookTitle = title
          merger.mergedBookAuthor = bookViewModel.bookAuthor

          merger.mergeBooks()
          val os = contentResolver.openOutputStream(uri)
          merger.writeBook(os)

          Toast.makeText(this, "Book saved to ${uri.path}", Toast.LENGTH_LONG).show()
          finish()
        }
      }
    }

    super.onActivityResult(requestCode, resultCode, data)
  }

  private fun finishWizard() {

    val title = bookViewModel.bookTitle

    val fileName = StringUtil.slugify(title, replacement = "_") + ".epub"
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "application/epub+zip"
      putExtra(Intent.EXTRA_TITLE, fileName)
    }

    startActivityForResult(intent, SAVE_FILE_ACTION)
  }

  private fun updateButtonsState(position: Int) {
    when (position) {
      0 -> {
        buttonNext.isEnabled = true
        buttonPrevious.isEnabled = false

        buttonNext.text = getString(R.string.wizard_next)
      }

      1 -> {
        buttonNext.isEnabled = true
        buttonPrevious.isEnabled = true

        buttonNext.text = getString(R.string.wizard_finish)
      }
    }
  }

  object StringUtil {
    fun slugify(word: String, replacement: String = "-") = Normalizer
      .normalize(word, Normalizer.Form.NFD)
      .replace("\\s+".toRegex(), replacement)
      .replace("\\/".toRegex(), replacement)
      .replace("\\\\".toRegex(), replacement)
  }
}