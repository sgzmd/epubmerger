package com.sigizmund.epubmergeapp

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import epubmerger.EpubProcessor
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.activityUiThreadWithContext
import org.jetbrains.anko.doAsync
import java.io.File
import java.nio.file.Paths


class MainActivity : AppCompatActivity() {

    val FILE_SELECT_CODE = 11
    val PERMISSION_REQUEST_CODE = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        val dialogProperties = DialogProperties()
        dialogProperties.selection_mode = DialogConfigs.MULTI_MODE
        dialogProperties.selection_type = DialogConfigs.FILE_SELECT
        dialogProperties.root = File(DialogConfigs.DEFAULT_DIR)
        dialogProperties.error_dir = File(DialogConfigs.DEFAULT_DIR)
        dialogProperties.offset = File(DialogConfigs.DEFAULT_DIR)
        dialogProperties.extensions = null

        val picker = FilePickerDialog(this, dialogProperties)
        picker.setDialogSelectionListener { files ->
            doAsync {
                val paths = files.map { Paths.get(it) }.toList()
                val processor = EpubProcessor(paths)
                processor.mergeFiles()
                val title = processor.book.title
                val fileName = title.replace(' ', '_').replace('/', '_')

                val downloads = Paths.get(Environment.getExternalStorageDirectory().absolutePath, "Download")
                val resultPath = Paths.get(downloads.toString(), "$fileName.epub")
                processor.writeBook(resultPath)

                activityUiThreadWithContext {
                    Toast.makeText(this, "Merged file saved to $resultPath", Toast.LENGTH_LONG).show()
                }
            }
        }
        picker.show()
    }
}
