package epubmerger

import nl.siegmann.epublib.domain.Book
import org.jsoup.Jsoup

object ResourceProcessor {
  private val SUPPORTED_SRC_ATTRS = listOf("src", "href")

  fun reprocessXhtmlFile(data: String, book: Book, index: Int, result: Book) {
    val soup = Jsoup.parse(data)
    val elements = soup.select("*")
    elements.forEach {
      for (attr in SUPPORTED_SRC_ATTRS) {
        if (it.hasAttr(attr)) {
          val href: String = it.attr(attr)
          if (book.resources.containsByHref(href)) {
            val res = book.resources.getByHref(href)

          }
        }
      }
    }
  }
}