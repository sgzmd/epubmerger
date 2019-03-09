package com.sigizmund.epubmergeapp

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import epubmerger.BookMerger
import kotlinx.android.synthetic.main.activity_book_merge_wizard.*
import java.io.File
import java.nio.file.Paths
import java.util.*

private const val NUM_PAGES = 3

class BookMergeWizardActivity :
  AppCompatActivity(),
  ReorderBooksFragment.BooksReoderListener,
  BookMetaFragment.OnFragmentInteractionListener  {

  override fun onBooksOrderChanged(entries: List<BookEntry>) {
    // model?.updateBooksOrder(entries)
    bookViewModel.bookEntries?.postValue(entries)
  }

  private val TAG = "BookMergeWizardActivity"

  override fun onMetadataUpdated(title: String, author: String) {
    bookViewModel.bookTitle = title
    bookViewModel.bookAuthor = author
  }


  lateinit var selectedFiles: ArrayList<String>
  private lateinit var bookViewModel: BooksViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_book_merge_wizard)

    buttonNext.setOnClickListener {
      if (viewPager.currentItem == 2) {
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
    selectedFiles = intent.extras.getStringArrayList(SELECTED_FILES)

    // For initial page it should be always disabled since this is the first page
    buttonPrevious.isEnabled = false

    this.bookViewModel = ViewModelProviders.of(
      this,
      BooksViewModel.BooksViewModelFactory(selectedFiles)
    )[BooksViewModel::class.java]

  }

  private fun finishWizard() {

    val dialogProperties = DialogProperties()
    dialogProperties.selection_mode = DialogConfigs.SINGLE_MODE
    dialogProperties.selection_type = DialogConfigs.DIR_SELECT
    dialogProperties.root = File(DialogConfigs.DEFAULT_DIR)

    dialogProperties.error_dir = File(DialogConfigs.DEFAULT_DIR)
    dialogProperties.offset = File(DialogConfigs.DEFAULT_DIR)
    dialogProperties.extensions = null

    val picker = FilePickerDialog(this, dialogProperties)
    picker.setDialogSelectionListener { directories: Array<out String> ->
      val merger = BookMerger(this.bookViewModel.bookEntries?.value?.map { it.book }!!)
      val title = bookViewModel.bookTitle

      merger.mergedBookTitle = title
      merger.mergedBookAuthor = bookViewModel.bookAuthor

      val fileName = title.replace(' ', '_').replace('/', '_')
      val resultPath = Paths.get("${directories.get(0)}/$fileName.epub")

      merger.mergeBooks()
      merger.writeBook(resultPath)

      Toast.makeText(this, "Book saved to $resultPath", Toast.LENGTH_LONG)

      // exiting activity.
      finish()
    }

    picker.show()
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

        buttonNext.text = getString(R.string.wizard_next)
      }

      2 -> {
        buttonNext.isEnabled = true
        buttonPrevious.isEnabled = true

        buttonNext.text = getString(R.string.wizard_finish)
      }
    }
  }

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

        2 -> {
          ReorderBooksFragment.newInstance(selectedFiles)
        }
        else -> {
          throw RuntimeException("Unsupported index")
        }
      }

      return item
    }
  }
}