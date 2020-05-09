import java.io.{File, PrintWriter}

import scala.io.{Codec, Source}

object UpdatePublicSuffixes {

  private val methodLength = 5000

  def generate(): Unit = {
    val source = Source.fromURL("https://publicsuffix.org/list/public_suffix_list.dat")
    generate(source.getLines.toList)
  }

  def generate(suffixLines: List[String]): Unit = {
    implicit val enc: Codec = Codec.UTF8

    val suffixes = for {
      line <- suffixLines
      trimLine = line.trim
      if !trimLine.startsWith("//") && !trimLine.isEmpty
    } yield trimLine

    val groups = suffixes.grouped(methodLength).zipWithIndex.map(_.swap).toMap

    val p = new PrintWriter(new File("shared/src/main/scala/io/lemonlabs/uri/inet/PublicSuffixes.scala"))
    p.println("package io.lemonlabs.uri.inet")
    p.println("")
    p.println("object PublicSuffixes {")
    p.println("  lazy val set = " + groups.keys.map(i => s"publicSuffixes$i").mkString(" ++ "))
    groups.foreach {
      case (index, group) =>
        val setArgs = group.map(suffix => s"""      "$suffix"""").mkString(",\n")
        p.println(s"  private def publicSuffixes$index =\n    Set(\n" + setArgs + "\n    )")
    }
    p.println("}")
    p.close()
  }
}
