import java.io.{File, PrintWriter}

import scala.io.{Codec, Source}

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
      case Nil => copy(wordEnd = true)
      case x :: xs =>
        next(x) match {
          case None =>
            this + (x -> Trie(xs))
          case Some(child) =>
            this + (x -> child.insert(xs))
        }
    }

    def size: Int = children.size + children.values.map(_.size).sum

    private val inlineLimit = 1250

    //Look into changing this to a scala macro?
    override def toString(): String = {
      def toString(charBreadcrumbs: String, t: Trie): String = {
        def toStringInline(t: Trie): String =
          trieString(t.children.mapValues(toStringInline), t.wordEnd)

        def trieString(children: Map[Char, String], wordEnd: Boolean): String = {
          s"""Trie(
            Map(${children.map(kv => s"'${kv._1}' -> ${kv._2}").mkString(",")})
            ${if (wordEnd) ", wordEnd = true" else ""}
          )
          """
        }

        val decl =
          if (charBreadcrumbs.isEmpty)
            "\tobject PublicSuffixTrie { lazy val publicSuffixTrie = "
          else
            s"\tprivate object $charBreadcrumbs { def t = "

        val body =
          if (t.size > inlineLimit) {
            trieString(t.children.map { case (ch, _) => ch -> s"${charBreadcrumbs}_${ch.toHexString}.t" }, t.wordEnd) +
              "}\n" +
              t.children
                .map {
                  case (ch, tChild) => toString(charBreadcrumbs + "_" + ch.toHexString, tChild)
                }
                .mkString("\n")
          } else {
            toStringInline(t) + "}\n"
          }

        decl + body
      }

      toString("", this)
    }
  }

  def generateTestVersion(): Unit = {
    generate(Iterator("com", "co.uk", "uk"))
  }

  def generate(): Unit = {
    val source = Source.fromURL("https://publicsuffix.org/list/public_suffix_list.dat")
    generate(source.getLines)
  }

  def generate(suffixLines: Iterator[String]): Unit = {
    implicit val enc = Codec.UTF8

    val suffixes = for {
      line <- suffixLines
      trimLine = line.trim
      if !trimLine.startsWith("//") && !trimLine.isEmpty
    } yield trimLine

    val trie = suffixes.foldLeft(Trie.empty) { (trieSoFar, suffix) =>
      trieSoFar insert suffix.reverse.toCharArray.toList
    }

    val p = new PrintWriter(new File("shared/src/main/scala/io/lemonlabs/uri/inet/PublicSuffixTrie.scala"))
    p.println("package io.lemonlabs.uri.inet")
    p.println("")
    p.println(trie.toString())
    p.close()
  }
}
