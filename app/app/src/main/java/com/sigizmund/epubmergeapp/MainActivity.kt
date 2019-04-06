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

    startActivity(Intent(this, BookMergeWizardActivity::class.java))
    finish()

    merge_files_button.setOnClickListener {
      startMerge()
    }
  }

  private fun setupPermissions() {

    val request = permissionsBuilder(
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
    ).build()

    request.listeners {
      onDenied { permissions ->
        Toast.makeText(
          this@MainActivity,
          "The app cannot work without these permissions",
          Toast.LENGTH_LONG
        ).show()
      }

      onPermanentlyDenied {
        Toast.makeText(
          this@MainActivity,
          "The app cannot work without these permissions. Please enable them manually in the app settings",
          Toast.LENGTH_LONG
        ).show()

        System.exit(0)
      }

      onAccepted { startMerge() }
    }

    request.send()
  }


  private fun startMerge() {

    if (ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
      ) != PERMISSION_GRANTED ||
      ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
      ) != PERMISSION_GRANTED
    ) {
      setupPermissions()
      return
    }

    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "application/epub+zip"
      putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
      putExtra("android.content.extra.SHOW_ADVANCED", true)
    }

    startActivityForResult(intent, FILE_SELECT_CODE)

//
//    val dialogProperties = DialogProperties()
//    dialogProperties.selection_mode = DialogConfigs.MULTI_MODE
//    dialogProperties.selection_type = DialogConfigs.FILE_SELECT
//    dialogProperties.root = File(DialogConfigs.DEFAULT_DIR)
//    dialogProperties.error_dir = File(DialogConfigs.DEFAULT_DIR)
//    dialogProperties.offset = File(DialogConfigs.DEFAULT_DIR)
//    dialogProperties.extensions = null
//
//    val picker = FilePickerDialog(this, dialogProperties)
//    picker.setDialogSelectionListener { files: Array<out String> ->
//      val intent = Intent(this, BookMergeWizardActivity::class.java)
//      val extras = Bundle()
//      extras.putStringArrayList(SELECTED_FILES, ArrayList(files.asList()))
//      intent.putExtras(extras)
//      startActivity(intent)
//    }
//    picker.show()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == FILE_SELECT_CODE) {
      if (data != null) {
        val clipData = data.clipData
        val fileList = ArrayList<Uri>()
        for (i in 0 until clipData.itemCount) {
          fileList.add(clipData.getItemAt(i).uri)
        }

        val intent = Intent(this, BookMergeWizardActivity::class.java)
        val extras = Bundle()
        extras.putParcelableArrayList(SELECTED_FILES, fileList)
        intent.putExtras(extras)
        startActivity(intent)
      }
    }
  }
}
