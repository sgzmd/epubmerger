package com.sigizmund.epubmergeapp

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import kotlinx.android.synthetic.main.activity_book_merge_wizard.*
import java.util.*

private const val NUM_PAGES = 3

class BookMergeWizardActivity : AppCompatActivity(), ReorderBooksFragment.OnFragmentInteractionListener {
  override fun onFragmentInteraction(uri: Uri) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  lateinit var selectedFiles: ArrayList<String>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_book_merge_wizard)

    viewPager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
    selectedFiles = intent.extras.getStringArrayList(SELECTED_FILES)
  }

  private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getCount(): Int = NUM_PAGES

    override fun getItem(position: Int) = ReorderBooksFragment.newInstance(selectedFiles)
  }
}