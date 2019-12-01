package com.sigizmund.epubmergeapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

  val TAG = "MainActivity"
  val FILE_SELECT_CODE = 11
  val PERMISSION_REQUEST_CODE = 12

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // TODO: I would love to know why I did it this way, but probably will keep it for now.
    startActivity(Intent(this, BookMergeWizardActivity::class.java))
    finish()
  }
}
