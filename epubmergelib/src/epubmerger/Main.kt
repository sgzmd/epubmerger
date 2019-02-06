package epubmerger

import nl.siegmann.epublib.epub.EpubReader
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun main(args: Array<String>) {
  if (args.size < 3) {
    println("Usage: java -jar /path/to/jar /path/to/source/directory/*.epub output.epub")
    return
  }

  val path = Paths.get(args[0])
  val stream: DirectoryStream<Path> = Files.newDirectoryStream(path.parent, path.fileName.toString())
  val files = stream.toList().sorted()
  val ep = BookMerger(files.map { EpubReader().readEpub(it.toFile().inputStream()) })
  ep.mergeBooks()

  ep.writeBook(Paths.get(args[2]))
}