package com.sigizmund.epubmergeapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_book_merge_wizard.*
import java.util.*

private const val NUM_PAGES = 3

class BookMergeWizardActivity : AppCompatActivity(), ReorderBooksFragment.OnFragmentInteractionListener {
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
    viewPager.currentItem = 0
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

    override fun getItem(position: Int): ReorderBooksFragment {
      Log.d(TAG, "getItem($position)")
      val item: ReorderBooksFragment = when (position) {
        0 -> {
          ReorderBooksFragment.newInstance(selectedFiles)
        }

        1 -> {
          ReorderBooksFragment.newInstance(selectedFiles)
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