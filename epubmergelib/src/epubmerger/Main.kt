package epubmerger

import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.SpineReference
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * We will be processing books in the following order:
 *
 * <ul>
 *     <li>Read the book</li>
 *     <li>Extract all resources</li>
 *     <li>Compute new names for resources so they don't clash</li>
 *     <li>Add resources to the new book with the name names, maintaining
 *         the link between the old and new name</li>
 *     <li>Add all spine references as per new resource names</li>
 * </ul>
 */

fun main4(args: Array<String>) {
  val epub = EpubReader().readEpub(
      File("data/Zlotnikov_Berserki_2_Boycy-s-okrainy-Galaktiki.173829.fb2.epub").inputStream(),
      Charsets.UTF_8.toString())

  print(epub.toString())
}


fun main(args: Array<String>) {


  if (args.size < 3) {
    println("Usage: java -jar /path/to/jar /path/to/source/directory *.epub output.epub")
    return
  }
  val stream: DirectoryStream<Path> = Files.newDirectoryStream(Paths.get(args[0]), args[1])
  val files = stream.toList().sorted()
  val ep = BookMerger(files.map { EpubReader().readEpub(it.toFile().inputStream()) })
  ep.mergeBooks()

  ep.writeBook(Paths.get(args[2]))
}

fun main2(args: Array<String>) {
  val book = Book()

  val stream: DirectoryStream<Path> = Files.newDirectoryStream(Paths.get("./" + args[0]), args[1])
  val epubs = stream.map { EpubReader().readEpub(it.toFile().inputStream()) }
  val title = epubs.map { it.title }
  println(title)

  book.metadata.titles = title
  // book.metadata.authors = epubs[0].metadata.authors

  var coverPageSet = false

  var index = 0
  for (epub in epubs) {

    if (!coverPageSet) {
      if (epub.coverImage != null && epub.coverPage != null) {
        book.coverImage = Resource(epub.coverImage.data, epub.coverImage.mediaType)

        val reprocessed = reprocessResource(epub.coverPage, epub.coverImage.href, book.coverImage.href)
        if (reprocessed != null) {
          book.coverPage = Resource(reprocessed, epub.coverPage.mediaType)
          coverPageSet = true
        }
      }
    }
    val sectionsMap = epub.spine.spineReferences.map {
      it.resource.href to String(it.resource.data)
    }.toMap()

    if (epub.tableOfContents.size() > 0) {
      val tocrefs = epub.tableOfContents.tocReferences
      for (tr in tocrefs) {
        val tc = TOCReference()
        copyToReference(tr, tc)
        book.tableOfContents.addTOCReference(tc)
      }
    }

    for (spineItem in epub.spine.spineReferences) {
      if (spineItem.resource.href == epub.coverPage.href) {
        continue
      }

      val href = spineItem.resource.href
//            val hrefPath = Paths.get(href)
//            val newHref = Paths.get( "${index}_${hrefPath.fileName}")
//            val newId = "${index}_${spineItem.resource.id}"
      val (newHref, newId) = makeNewHrefAndId(index, href, spineItem.resource.id)
      val resource = Resource(newId, spineItem.resource.data, newHref.toString(), spineItem.resource.mediaType)
      val spr = SpineReference(resource)

      book.resources.add(resource)
      book.spine.addSpineReference(spr)
    }
    ++index
  }

//  EpubWriter().write(book, File("result.epub").outputStream())
}

fun makeNewHrefAndId(index: Int, href: String, id: String): Pair<String, String> {
  val hrefPath = Paths.get(href)
  val newHref = Paths.get( "${index}_${hrefPath.fileName}")
  val newId = "${index}_${id}"

  return Pair(newHref.toString(), newId)
}

fun copyToReference(copyFrom: TOCReference, copyTo: TOCReference) {
  copyTo.title = copyFrom.title
  if (copyFrom.resource != null) {
    copyTo.resource = copyFrom.resource
  }

  if (copyFrom.children != null) {
    for (c in copyFrom.children) {
      val tr = TOCReference()
      copyToReference(c, tr)
      copyTo.addChildSection(tr)
    }
  }
}

fun reprocessResource(res: Resource, originalHref: String, newHref: String): ByteArray? {
  if (isXHTML(res.mediaType.name)) {
    val xhtml = String(res.data)
    val newXhtml = xhtml.split(originalHref).joinToString(newHref)
    return newXhtml.toByteArray()
  } else {
    return null
  }
}

fun isXHTML(mediaType: String) = mediaType.toLowerCase().contains("xhtml")
    || mediaType.toLowerCase().contains("xml")
    || mediaType.toLowerCase().contains("xml")