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

    val privateDomainsStart =
      suffixLines.indexWhere(line => line.startsWith("//") && line.contains("===BEGIN PRIVATE DOMAINS==="))

    if (privateDomainsStart <= 0 || privateDomainsStart >= suffixLines.size) {
      println("Can't find the private domains section in the public suffix list")
      sys.exit(1)
    }

    val publicDomainSuffixes = for {
      line <- suffixLines.slice(0, privateDomainsStart)
      trimLine = line.trim
      if !trimLine.startsWith("//") && trimLine.nonEmpty
    } yield trimLine

    val (exceptions, others) = publicDomainSuffixes.partition(_.startsWith("!"))
    val (wildcards, suffixes) = others.partition(_.contains("*"))
    val (wildcardPrefixes, otherWildcards) = wildcards.partition(_.startsWith("*"))

    if (otherWildcards.nonEmpty) {
      println("We have non-prefix wildcards! We need to implement this!!!")
      println(otherWildcards)
      sys.exit(1)
    }

    val groups = suffixes.grouped(methodLength).zipWithIndex.map(_.swap).toMap

    val p = new PrintWriter(new File("shared/src/main/scala/io/lemonlabs/uri/inet/PublicSuffixes.scala"))
    p.println("package io.lemonlabs.uri.inet")
    p.println("")
    p.println("object PublicSuffixes {")

    p.println("  lazy val exceptions = Set(")
    p.println(exceptions.map(_.tail).map(e => s"""    "$e"""").mkString(",\n"))
    p.println("  )\n")

    p.println("  lazy val wildcardPrefixes = Set(")
    p.println(wildcardPrefixes.map(_.drop(2)).map(w => s"""    "$w"""").mkString(",\n"))
    p.println("  )\n")

    p.println("  lazy val set = " + groups.keys.map(i => s"publicSuffixes$i").mkString(" ++ "))
    groups.foreach { case (index, group) =>
      val setArgs = group.map(suffix => s"""      "$suffix"""").mkString(",\n")
      p.println(s"  private def publicSuffixes$index =\n    Set(\n" + setArgs + "\n    )")
    }
    p.println("}")
    p.close()
  }
}
