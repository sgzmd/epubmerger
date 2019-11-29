package epubmerger

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import nl.siegmann.epublib.epub.EpubReader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

class MergeArgs(parser: ArgParser) {
  val epubs by parser.positionalList("SOURCE", "Epub files to merge", 1..Int.MAX_VALUE)
      .addValidator {
        if (value.size <= 1) {
          throw InvalidArgumentException("One expects more than one epub to merge")
        }
      }
  val output by parser.storing("-o", "--output", help = "Output epub path")
  val author by parser.storing("-A", "--author", help = "Merged book author name").default("")
  val title by parser.storing("-T", "--title", help = "Title of the merged book").default("")
}

fun main(args: Array<String>) {
  ArgParser(args).parseInto(::MergeArgs).run {
    println("Merging files: ${epubs.joinToString(",")}")

    val ep = BookMerger(epubs
        .map { Paths.get(it) }
        .map { EpubReader().readEpub(it.toFile().inputStream()) })

    if (!title.isNullOrBlank()) {
      ep.mergedBookTitle = title
    }

    if (!author.isNullOrBlank()) {
      ep.mergedBookAuthor = author
    }

    ep.mergeBooks()
    ep.writeBook(Paths.get(output))
  }
}
