package epubmerger

import nl.siegmann.epublib.domain.*
import nl.siegmann.epublib.epub.EpubReader
import nl.siegmann.epublib.epub.EpubWriter
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.*

/**
 * Joins multiple EPub files into a single file. This needs much refactoring.
 */
class EpubProcessor(files: List<Path>) {

  private val LOG = LoggerFactory.getLogger(EpubProcessor.javaClass)
  private val VERSION = "1.0"
  private val PUBLISHER = "EpubMerger $VERSION"

  private var files: List<Path> = files
  internal var book = Book()
  internal lateinit var hrefIdMap: Map<String, EpubResource>

  fun mergeFiles() {
    val epubs = readFiles()
    mergeBooks(epubs)
  }

  fun mergeBooks(epubs: List<Book>) {
    hrefIdMap = calculateResourceNames(epubs)
    reprocessResources(epubs)

    var idx = 0
    for (epub in epubs) {
      if (epub.coverPage != null && epub.coverImage != null) {
        buildCoverPage(epub, idx)
        break
      } else {
        ++idx
      }
    }

    generateTocNaive(epubs)
    generateMetadata(epubs)
  }

  private fun generateTocNaive(epubs: List<Book>) {
    epubs.forEachIndexed { index, epub ->
      LOG.info("Generating TOC from ${epub.title} (idx $index)")
      val ro = findResourceObject(epub.coverPage.href, index)
      val res = book.resources.getByHref(ro?.newHref)

      val topLevelSection = book.tableOfContents.addTOCReference(TOCReference(epub.title, res))

      if (book.spine.findFirstResourceById(res.id) < 0) {
        book.spine.addSpineReference(SpineReference(res))
      }

      epub.tableOfContents.tocReferences.forEach {
        LOG.info("Processing TOC reference ${it.title}")
        addTocRef(it, topLevelSection, index)
      }
    }
  }

  private fun findResourceObject(href: String, idx: Int): EpubResource? {
    return hrefIdMap[EpubResource.makeKey(idx, href)]
  }

  fun writeBook(path: Path) {
    EpubWriter().write(book, path.toFile().outputStream())
  }

  fun generateMetadata(epubs: List<Book>) {
    book.metadata.authors.clear()
    book.metadata.authors.addAll( epubs.map { it.metadata.authors }.flatten<Author?>().distinct() )

    book.metadata.titles.clear()
    val allTitles = epubs.map { it.metadata.titles }.flatten<String?>()
    val series = allTitles.joinToString("; ")
    book.metadata.titles.addAll(allTitles)
    book.metadata.titles.add(series)

    book.metadata.addIdentifier(Identifier("uuid", UUID.randomUUID().toString()))
    val publishers = epubs.map { it.metadata.publishers }.flatten().toSet()
    book.metadata.publishers.addAll(publishers)
    book.metadata.addPublisher(PUBLISHER)
  }

  internal fun calculateResourceNames(epubs: List<Book>): HashMap<String, EpubResource> {
    // Old href to new href/id
    LOG.info("Calculating new resource names for ${epubs.size} books")
    val map = HashMap<String, EpubResource>()
    epubs.forEachIndexed { index, epub ->
      epub.resources.all.filterNot {
        isToc(it.mediaType.toString())
      }.forEachIndexed { resIdx, resource ->
        val ext = getFileExtension(resource.href)
        val id = "id_${index}_${resIdx}"
        val href = "href_${index}_${resIdx}.$ext"

        LOG.info("In ${epub.title} assigning ${resource.href} new href=$href and id=$id")

        val ro = EpubResource(resource.href, index)
        ro.newHref = href
        ro.newId = id
        map.put(ro.key(), ro)
      }
    }

    return map
  }

  internal fun reprocessResources(epubs: List<Book>) {
    LOG.info("Reprocessing resources for ${epubs.size} books")
    epubs.forEachIndexed { index, epub ->
      epub.resources.all.filterNot {
        isToc(it.mediaType.toString())
      }.forEach { res ->
        LOG.info("For book ${epub.title} reprocessing ${res.href}")

        val hrefIdPair = hrefIdMap[EpubResource.makeKey(index, res.href)]
        LOG.debug("${res.href} => $hrefIdPair")
        val r = book.addResource(Resource(
            hrefIdPair?.newId,
            reprocessResourceData(res.data, res.mediaType.toString()),
            hrefIdPair?.newHref,
            res.mediaType))

        LOG.debug("New resource was added as id=${r.id} href=${r.href}")
      }
    }
  }


  internal fun addTocRef(originalTocRef: TOCReference, bookSection: TOCReference, idx: Int) {
    val title = originalTocRef.title
    val resHref = originalTocRef.resource.href
    val key = EpubResource.makeKey(idx, resHref)
    LOG.info("addTocRef title=$title resHref=$resHref key=$key")
    val ro = hrefIdMap[key]
    val newResource = book.resources.getByHref(ro?.newHref)
    val childSection = bookSection.addChildSection(TOCReference(title, newResource))

    if (book.spine.findFirstResourceById(newResource.id) < 0) {
      book.spine.addSpineReference(SpineReference(newResource))
    }


    if (originalTocRef.children != null && !originalTocRef.children.isEmpty()) {
      originalTocRef.children.forEach { addTocRef(it, childSection, idx) }
    }
  }

  internal fun reprocessResourceData(data: ByteArray, type: String): ByteArray {
    if (SUPPORTED_MEDIA_TYPES.contains(type)) {
      // This is a text type we understand, to a degree, OK to reprocess
      val str = String(data, Charset.forName("UTF-8"))

      // Very naive and non-efficient implementation for now
      var result = str
      for (key in hrefIdMap.keys) {
        val res = hrefIdMap[key]
        val quotedHref = "\"${res?.originalHref}\""
        val newQuotedHref = "\"${hrefIdMap[key]?.newHref}\""
        while (result.indexOf(quotedHref) > 0) {
          result = result.replace(quotedHref, newQuotedHref)
        }
      }

      return result.toByteArray()
    } else {
      return data
    }
  }


  internal fun buildCoverPage(epub: Book, idx: Int) {
    assert(hrefIdMap != null, { "hrefIdMap must be initialised" })
    val newImageHrefId = hrefIdMap[EpubResource.makeKey(idx, epub.coverImage.href)]
    val newCoverPageHrefId = hrefIdMap[EpubResource.makeKey(idx, epub.coverPage.href)]

    book.coverImage = book.resources.getByHref(newImageHrefId?.newHref)
    book.coverPage = book.resources.getByHref(newCoverPageHrefId?.newHref)
  }

  internal fun readFiles(): List<Book> {
    return this.files.map {
      LOG.info("Loading file $it")
      EpubReader().readEpub(it.toFile().inputStream())
    }
  }

  companion object {
    internal fun getFileExtension(fileName: String): String {
      val dotIdx = fileName.indexOf('.')
      if (dotIdx >= 0) {
        return fileName.substring(dotIdx + 1)
      } else {
        return ""
      }
    }

    internal fun isToc(mediaType: String) : Boolean {
      return TOC_TYPE.contains(mediaType)
    }

    val SUPPORTED_MEDIA_TYPES = setOf("text/html", "application/xhtml+xml", "text/plain", "text/xml", "application/xml")

    val TOC_TYPE = setOf("application/x-dtbncx+xml")
  }
}