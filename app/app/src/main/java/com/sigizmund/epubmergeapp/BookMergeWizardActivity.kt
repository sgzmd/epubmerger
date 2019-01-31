package com.sigizmund.epubmergeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import kotlinx.android.synthetic.main.activity_book_merge_wizard.*

private const val NUM_PAGES = 3

class BookMergeWizardActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_book_merge_wizard)

    viewPager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
  }
}

private class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
  override fun getCount(): Int = NUM_PAGES

  // TODO: this will fail on creation
  override fun getItem(position: Int): Fragment = ReorderBooksFragment()
}