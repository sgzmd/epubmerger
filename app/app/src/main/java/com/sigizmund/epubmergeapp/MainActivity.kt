package com.sigizmund.epubmergeapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import epubmerger.EpubProcessor

import kotlinx.android.synthetic.main.activity_main.*
import lib.folderpicker.FolderPicker
import org.jetbrains.anko.activityUiThreadWithContext
import org.jetbrains.anko.doAsync
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
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
                Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED ) {
            setupPermissions()
            return
        }

        val intent = Intent(this, FolderPicker::class.java)
        intent.putExtra("title", "Select files to merge")
        intent.putExtra("pickFiles", true)

        startActivityForResult(intent, FILE_SELECT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_SELECT_CODE) {
            val a = data?.extras
            doAsync {
                val downloads = Paths.get(Environment.getExternalStorageDirectory().absolutePath, "Download")
                return@doAsync
                val stream: DirectoryStream<Path> = Files.newDirectoryStream(downloads, "*.epub")
                val files = stream.toList()

                val processor = EpubProcessor(files)
                processor.mergeFiles()
                val result = Paths.get(downloads.toString(), "result.epub")
                processor.writeBook(result)

                activityUiThreadWithContext {
                    Toast.makeText(this, "File was produced", Toast.LENGTH_LONG)
                }

            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

}
