package com.sigizmund.epubmergeapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_book_merge_wizard.*
import java.util.*

private const val NUM_PAGES = 3

class BookMergeWizardActivity :
  AppCompatActivity(),
  ReorderBooksFragment.OnFragmentInteractionListener,
  BookMetaFragment.OnFragmentInteractionListener  {

  private lateinit var title: String
  private lateinit var author: String

  override fun onMetadataUpdated(title: String, author: String) {
    this.title = title
    this.author = author
  }

  private val TAG = "BookMergeWizardActivity"

  override fun onFragmentInteraction(uri: Uri) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  lateinit var selectedFiles: ArrayList<String>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_book_merge_wizard)

    buttonNext.setOnClickListener {
      viewPager.currentItem++
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
  }

  private fun updateButtonsState(position: Int) {
    when (position) {
      0 -> {
        buttonNext.isEnabled = true
        buttonPrevious.isEnabled = false
      }

      1 -> {
        buttonNext.isEnabled = true
        buttonPrevious.isEnabled = true
      }

      2 -> {
        buttonNext.isEnabled = false
        buttonPrevious.isEnabled = true
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
          BookMetaFragment.newInstance("Title", "Author")
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