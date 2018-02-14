package com.netaporter.uri.inet

import scala.annotation.tailrec

object Trie {
  val Empty = Trie(Map.empty)
}

case class Trie(children: Map[Char, Trie], wordEnd: Boolean = false) {

  def next(c: Char): Option[Trie] =
    children.get(c)

  def matches(s: String): Vector[String] = {
    @tailrec def collectMatches(previous: String, stillToGo: List[Char], current: Trie, matches: Vector[String]): Vector[String] = stillToGo match {
      case Nil =>
        matches
      case x :: xs =>
        current.next(x) match {
          case None =>
            matches
          case Some(next) =>
            val newPrevious = previous + x
            val newMatches = if(next.wordEnd && xs.headOption.contains('.')) newPrevious +: matches else matches
            collectMatches(newPrevious, xs, next, newMatches)
        }
    }
    collectMatches("", s.toCharArray.toList, this, Vector.empty)
  }

  def longestMatch(s: String): Option[String] =
    matches(s).headOption
}
