package com.sigizmund.epubmergeapp

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import epubmerger.EpubProcessor

import kotlinx.android.synthetic.main.activity_main.*
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        merge_files_button.setOnClickListener {
            thread {
                val downloads = Paths.get(Environment.getExternalStorageDirectory().absolutePath, "Download")
                val stream: DirectoryStream<Path> = Files.newDirectoryStream(downloads, "*.epub")
                val files = stream.toList()

                val processor = EpubProcessor(files)
                processor.mergeFiles()
                val result = Paths.get(downloads.toString(), "result.epub")
                processor.writeBook(result)

                Toast.makeText(this, "File was produced", Toast.LENGTH_LONG)
            }
        }
    }
}
