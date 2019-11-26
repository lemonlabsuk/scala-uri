package io.lemonlabs.uri.typesafe

import cats.{Contravariant, Eq}
import simulacrum.typeclass
import cats.syntax.contravariant._

import scala.language.implicitConversions

@typeclass trait Fragment[A] {
  def fragment(a: A): String
}

object Fragment extends FragmentInstances

sealed trait FragmentInstances2 {
  implicit val contravariant: Contravariant[Fragment] = new Contravariant[Fragment] {
    def contramap[A, B](fa: Fragment[A])(f: B => A): Fragment[B] = b => fa.fragment(f(b))
  }
}

sealed trait FragmentInstances1 extends FragmentInstances2 {
  implicit val stringFragment: Fragment[String] = a => a
  implicit final val booleanQueryValue: Fragment[Boolean] = stringFragment.contramap(_.toString)
  implicit final val charQueryValue: Fragment[Char] = stringFragment.contramap(_.toString)
  implicit final val intQueryValue: Fragment[Int] = stringFragment.contramap(_.toString)
  implicit final val longQueryValue: Fragment[Long] = stringFragment.contramap(_.toString)
  implicit final val floatQueryValue: Fragment[Float] = stringFragment.contramap(_.toString)
  implicit final val doubleQueryValue: Fragment[Double] = stringFragment.contramap(_.toString)
  implicit final val uuidQueryValue: Fragment[java.util.UUID] = stringFragment.contramap(_.toString)
  implicit val noneFragment: Fragment[None.type] = _ => ""
}

sealed trait FragmentInstances extends FragmentInstances1 {
  implicit def optionFragment[A: Fragment]: Fragment[Option[A]] = _.map(Fragment[A].fragment).getOrElse("")
}
