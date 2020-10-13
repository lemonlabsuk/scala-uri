package io.lemonlabs.uri.typesafe

import cats.Contravariant
import simulacrum.typeclass

import scala.language.implicitConversions
import scala.annotation.implicitNotFound

@implicitNotFound("Could not find an instance of Fragment for ${A}")
@typeclass trait Fragment[A] extends Serializable { self =>
  def fragment(a: A): Option[String]
  def contramap[B](f: B => A): Fragment[B] = (b: B) => self.fragment(f(b))
}

object Fragment extends FragmentInstances {
  /* ======================================================================== */
  /* THE FOLLOWING CODE IS MANAGED BY SIMULACRUM; PLEASE DO NOT EDIT!!!!      */
  /* ======================================================================== */

  /** Summon an instance of [[Fragment]] for `A`.
    */
  @inline def apply[A](implicit instance: Fragment[A]): Fragment[A] = instance

  object ops {
    implicit def toAllFragmentOps[A](target: A)(implicit tc: Fragment[A]): AllOps[A] {
      type TypeClassType = Fragment[A]
    } =
      new AllOps[A] {
        type TypeClassType = Fragment[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  trait Ops[A] extends Serializable {
    type TypeClassType <: Fragment[A]
    def self: A
    val typeClassInstance: TypeClassType
    def fragment: Option[String] = typeClassInstance.fragment(self)
  }
  trait AllOps[A] extends Ops[A]
  trait ToFragmentOps extends Serializable {
    implicit def toFragmentOps[A](target: A)(implicit tc: Fragment[A]): Ops[A] {
      type TypeClassType = Fragment[A]
    } =
      new Ops[A] {
        type TypeClassType = Fragment[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  object nonInheritedOps extends ToFragmentOps

  /* ======================================================================== */
  /* END OF SIMULACRUM-MANAGED CODE                                           */
  /* ======================================================================== */

}

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
