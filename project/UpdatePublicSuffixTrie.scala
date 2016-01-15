import java.io.{PrintWriter, File}

import scala.io.{Codec, Source}

import spray.json.DefaultJsonProtocol._
import spray.json._

object UpdatePublicSuffixTrie {
  object Trie {
    def apply(prefix: List[Char]): Trie = prefix match {
      case Nil     => Trie(Map.empty, wordEnd = true)
      case x :: xs => Trie(Map(x -> Trie(xs)), wordEnd = false)
    }

    def empty = Trie(Map.empty, wordEnd = false)
  }
  case class Trie(children: Map[Char, Trie], wordEnd: Boolean = false) {

    def +(kv: (Char, Trie)): Trie =
      this.copy(children = children + kv)

    def next(ch: Char): Option[Trie] =
      children.get(ch)

    def insert(value: List[Char]): Trie = value match {
      case Nil     => copy(wordEnd = true)
      case x :: xs =>
        next(x) match {
          case None =>
            this + (x -> Trie(xs))
          case Some(child) =>
            this + (x -> child.insert(xs))
        }

    }

    override def toString(): String = {
      s"""Trie(
            Map(${children.map(kv => s"'${kv._1}' -> ${kv._2.toString()}").mkString(",")})
            ${if(wordEnd) ", wordEnd = true" else ""}
          )
        """
    }

  }

  def generate(): Unit = {
    implicit val enc = Codec.UTF8

    val suffixes = for {
      line <- Source.fromURL("https://publicsuffix.org/list/public_suffix_list.dat").getLines
      trimLine = line.trim
      if !trimLine.startsWith("//") && !trimLine.isEmpty
    } yield trimLine

    val trie = suffixes.foldLeft(Trie.empty) { (trieSoFar, suffix) =>
      trieSoFar insert suffix.reverse.toCharArray.toList
    }

    // Reduce JSON file size by using short names "c" and "e" for children and wordEnd
    implicit lazy val trieFmt: JsonFormat[Trie] = lazyFormat(jsonFormat(Trie.apply, "c", "e"))

    val p = new PrintWriter(new File("src/main/resources/public_suffix_trie.json"))
    p.println(trie.toJson.compactPrint)
    p.close()
  }
}