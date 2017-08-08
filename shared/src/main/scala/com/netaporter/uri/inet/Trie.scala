package com.netaporter.uri.inet

import scala.annotation.tailrec

object Trie {
  val Empty = Trie(Map.empty)
}

case class Trie(children: Map[Char, Trie], wordEnd: Boolean = false) {

  def next(c: Char) =
    children.get(c)

  def matches(s: String): Seq[String] = {
    @tailrec def collectMatches(previous: String, stillToGo: List[Char], current: Trie, matches: Seq[String]): Seq[String] = stillToGo match {
      case Nil =>
        matches
      case x :: xs =>
        current.next(x) match {
          case None =>
            matches
          case Some(next) =>
            val newPrevious = previous + x
            //TODO: When Scala 2.10 support is dropped, change headOption == Some to headOption.contains
            val newMatches = if(next.wordEnd && xs.headOption == Some('.')) newPrevious +: matches else matches
            collectMatches(newPrevious, xs, next, newMatches)
        }
    }
    collectMatches("", s.toCharArray.toList, this, Seq.empty)
  }

  def longestMatch(s: String): Option[String] =
    matches(s).headOption
}
