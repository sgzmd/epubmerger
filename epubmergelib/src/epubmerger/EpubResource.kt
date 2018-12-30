package epubmerger

/**
 * Represents a link between original href/id and new generated href and id
 * for any resource in an epub file.
 */
class EpubResource(href: String, idx: Int) {
  val originalHref = href
  val bookIndex = idx
  var newHref : String? = null
  var newId : String? = null

  fun key(): String {
    return makeKey(bookIndex, originalHref)
  }

  companion object {
    fun makeKey(idx: Int, href: String): String {
      return "${idx}_${href}"
    }
  }
}