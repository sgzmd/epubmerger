package epubmerger

import nl.siegmann.epublib.epub.EpubReader
import java.nio.file.Paths

fun main(args: Array<String>) {
  if (args.size < 3) {
    println("Usage: java -jar /path/to/jar output.epub /path/to/source/directory/file1.epub /path/to/source/directory/file2.epub ... ")
    return
  }

  val outputFileName = args[0]
  val fileNames = args.toList().drop(1)

  println(fileNames)

  val ep = BookMerger(fileNames
    .map { Paths.get(it) }
    .map { EpubReader().readEpub(it.toFile().inputStream()) })
  ep.mergeBooks()

  ep.writeBook(Paths.get(outputFileName))
}