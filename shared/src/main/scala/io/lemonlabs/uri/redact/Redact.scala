package io.lemonlabs.uri.redact

import io.lemonlabs.uri.Url
import io.lemonlabs.uri.typesafe.QueryKey

import QueryKey.ops._

object Redact {
  def byRemoving: RedactByRemoving = RedactByRemoving(identity)
  def withPlaceholder(placeholder: String): RedactByReplacing = RedactByReplacing(placeholder, identity)
}

trait Redactor {
  def apply(u: Url): Url
}

case class RedactByRemoving(f: Url => Url) extends Redactor {

  private def andThen(next: Url => Url) = copy(f = f.andThen(next))

  def allParams(): RedactByRemoving = andThen(_.removeQueryString())
  def params[K: QueryKey](names: K*): RedactByRemoving = andThen(_.removeParams(names))
  def userInfo(): RedactByRemoving = andThen(_.removeUserInfo())
  def password(): RedactByRemoving = andThen(_.removePassword())

  def apply(u: Url): Url = f(u)
}

case class RedactByReplacing(placeholder: String, f: Url => Url) extends Redactor {

  private def andThen(next: Url => Url) = copy(f = f.andThen(next))

  def allParams(): RedactByReplacing = andThen(_.mapQueryValues(_ => placeholder))
  def params[K: QueryKey](names: K*): RedactByReplacing = {
    val namesStr = names.map(_.queryKey)
    andThen { url =>
      url.mapQuery {
        case (k, _) if namesStr.contains(k) => k -> placeholder
      }
    }
  }
  def user(): RedactByReplacing = andThen(_.mapUser(_ => placeholder))
  def password(): RedactByReplacing = andThen(_.mapPassword(_ => placeholder))

  def apply(u: Url): Url = f(u)
}
