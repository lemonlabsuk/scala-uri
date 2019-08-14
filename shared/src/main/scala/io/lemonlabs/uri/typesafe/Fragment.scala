package io.lemonlabs.uri.typesafe

import simulacrum.typeclass
import scala.language.implicitConversions

@typeclass trait Fragment[A] {
  def fragment(a: A): String
}

object Fragment extends FragmentInstances

trait FragmentInstances1 {
  implicit val stringFragment: Fragment[String] = a => a
  implicit val noneFragment: Fragment[None.type] = _ => ""
}

trait FragmentInstances extends FragmentInstances1 {
  implicit def optionFragment[A: Fragment]: Fragment[Option[A]] = _.map(Fragment[A].fragment).getOrElse("")
}
