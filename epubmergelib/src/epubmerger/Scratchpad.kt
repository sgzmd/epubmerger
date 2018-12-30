package epubmerger

import nl.siegmann.epublib.epub.EpubReader

object Scratchpad {

  fun main(args: List<String>) {
    val epub = EpubReader().readEpubLazy(
        "data/Zlotnikov_Berserki_1_Myatezh-na-okraine-Galaktiki.173825.fb2.epub",
        Charsets.UTF_8.toString())

    print(epub.toString())
  }
}