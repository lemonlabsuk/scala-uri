package io.lemonlabs.uri.typesafe

import cats.Contravariant
import simulacrum.typeclass

import scala.language.implicitConversions

@typeclass trait Fragment[A] { self =>
  def fragment(a: A): Option[String]
  def contramap[B](f: B => A): Fragment[B] = (b: B) => self.fragment(f(b))
}

object Fragment extends FragmentInstances

sealed trait FragmentInstances2 {
  implicit val contravariant: Contravariant[Fragment] = new Contravariant[Fragment] {
    def contramap[A, B](fa: Fragment[A])(f: B => A): Fragment[B] = b => fa.fragment(f(b))
  }
}

sealed trait FragmentInstances1 extends FragmentInstances2 {
  implicit val stringFragment: Fragment[String] = a => Option(a)
  implicit final val booleanQueryValue: Fragment[Boolean] = stringFragment.contramap(_.toString)
  implicit final val charQueryValue: Fragment[Char] = stringFragment.contramap(_.toString)
  implicit final val intQueryValue: Fragment[Int] = stringFragment.contramap(_.toString)
  implicit final val longQueryValue: Fragment[Long] = stringFragment.contramap(_.toString)
  implicit final val floatQueryValue: Fragment[Float] = stringFragment.contramap(_.toString)
  implicit final val doubleQueryValue: Fragment[Double] = stringFragment.contramap(_.toString)
  implicit final val uuidQueryValue: Fragment[java.util.UUID] = stringFragment.contramap(_.toString)
  implicit val noneFragment: Fragment[None.type] = identity
}

sealed trait FragmentInstances extends FragmentInstances1 {
  implicit def optionFragment[A: Fragment]: Fragment[Option[A]] = _.flatMap(Fragment[A].fragment)
}
