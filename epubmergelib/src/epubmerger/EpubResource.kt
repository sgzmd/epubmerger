package epubmerger

/**
 * Represents a link between original href/id and new generated href and id
 * for any resource in an epub file.
 */
data class EpubResource(val originalHref: String, val bookIndex: Int, var newHref: String?, var newId: String?) {
  companion object {
    // TODO delete me
    fun makeKey(idx: Int, res: String): String = ""
  }
}