package io.lemonlabs.uri.typesafe

import java.util.UUID

import cats.Contravariant
import cats.syntax.contravariant._
import shapeless._
import shapeless.labelled._
import simulacrum.typeclass

import scala.language.implicitConversions
import scala.annotation.implicitNotFound

@implicitNotFound("Could not find an instance of PathPart for ${A}")
@typeclass trait PathPart[-A] extends Serializable {
  def path(a: A): String
  def splitPath(a: A): Seq[String] = path(a).split('/').toSeq
}

object PathPart extends PathPartInstances {
  /* ======================================================================== */
  /* THE FOLLOWING CODE IS MANAGED BY SIMULACRUM; PLEASE DO NOT EDIT!!!!      */
  /* ======================================================================== */

  /**
    * Summon an instance of [[PathPart]] for `A`.
    */
  @inline def apply[A](implicit instance: PathPart[A]): PathPart[A] = instance

  object ops {
    implicit def toAllPathPartOps[A](target: A)(implicit tc: PathPart[A]): AllOps[A] {
      type TypeClassType = PathPart[A]
    } =
      new AllOps[A] {
        type TypeClassType = PathPart[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  trait Ops[A] extends Serializable {
    type TypeClassType <: PathPart[A]
    def self: A
    val typeClassInstance: TypeClassType
    def path: String = typeClassInstance.path(self)
    def splitPath: Seq[String] = typeClassInstance.splitPath(self)
  }
  trait AllOps[A] extends Ops[A]
  trait ToPathPartOps extends Serializable {
    implicit def toPathPartOps[A](target: A)(implicit tc: PathPart[A]): Ops[A] {
      type TypeClassType = PathPart[A]
    } =
      new Ops[A] {
        type TypeClassType = PathPart[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  object nonInheritedOps extends ToPathPartOps

  /* ======================================================================== */
  /* END OF SIMULACRUM-MANAGED CODE                                           */
  /* ======================================================================== */

}

sealed trait PathPartInstances2 {
  implicit val contravariant: Contravariant[PathPart] = new Contravariant[PathPart] {
    def contramap[A, B](fa: PathPart[A])(f: B => A): PathPart[B] = b => fa.path(f(b))
  }
}

sealed trait PathPartInstances1 extends PathPartInstances2 {
  implicit val stringPathPart: PathPart[String] = a => a
  implicit val booleanPathPart: PathPart[Boolean] = stringPathPart.contramap(_.toString)
  implicit val charPathPart: PathPart[Char] = stringPathPart.contramap(_.toString)
  implicit val intPathPart: PathPart[Int] = stringPathPart.contramap(_.toString)
  implicit val longPathPart: PathPart[Long] = stringPathPart.contramap(_.toString)
  implicit val floatPathPart: PathPart[Float] = stringPathPart.contramap(_.toString)
  implicit val doublePathPart: PathPart[Double] = stringPathPart.contramap(_.toString)
  implicit val uuidPathPart: PathPart[UUID] = stringPathPart.contramap(_.toString)
}

sealed trait PathPartInstances extends PathPartInstances1 {
  implicit def optionPathPart[A: PathPart]: PathPart[Option[A]] = a => a.map(PathPart[A].path).getOrElse("")
}

sealed trait TraversablePathPartsInstances {
  implicit def singleTraversablePathParts[A](implicit tc: PathPart[A]): TraversablePathParts[A] =
    a => tc.splitPath(a)

  implicit def iterableTraversablePathParts[A](implicit tc: PathPart[A]): TraversablePathParts[Iterable[A]] =
    (ax: Iterable[A]) => ax.flatMap(tc.splitPath).toSeq

  implicit def seqTraversablePathParts[A](implicit tc: PathPart[A]): TraversablePathParts[Seq[A]] =
    (ax: Seq[A]) => ax.flatMap(tc.splitPath)

  implicit def vectorTraversablePathParts[A](implicit tc: PathPart[A]): TraversablePathParts[Vector[A]] =
    (ax: Vector[A]) => ax.flatMap(tc.splitPath)

  implicit def listTraversablePathParts[A](implicit tc: PathPart[A]): TraversablePathParts[List[A]] =
    (ax: List[A]) => ax.flatMap(tc.splitPath)
}

@implicitNotFound("Could not find an instance of TraversablePathParts for ${A}")
@typeclass trait TraversablePathParts[A] extends Serializable {
  def toSeq(a: A): Seq[String]
  def toVector(a: A): Vector[String] =
    toSeq(a).toVector
}

object TraversablePathParts extends TraversablePathPartsInstances {
  implicit def field[K <: Symbol, V](implicit
      K: Witness.Aux[K],
      V: PathPart[V]
  ): TraversablePathParts[FieldType[K, V]] =
    (a: FieldType[K, V]) => V.splitPath(a)

  implicit def sub[K <: Symbol, V](implicit
      K: Witness.Aux[K],
      V: TraversablePathParts[V]
  ): TraversablePathParts[FieldType[K, V]] =
    (a: FieldType[K, V]) => V.toSeq(a)

  implicit val hnil: TraversablePathParts[HNil] =
    (_: HNil) => Seq.empty

  implicit def hcons[H, T <: HList](implicit
      H: TraversablePathParts[H],
      T: TraversablePathParts[T]
  ): TraversablePathParts[H :: T] =
    (a: H :: T) => H.toSeq(a.head) ++ T.toSeq(a.tail)

  def product[A, R <: HList](implicit gen: Generic.Aux[A, R], R: TraversablePathParts[R]): TraversablePathParts[A] =
    (a: A) => R.toSeq(gen.to(a))

  /* ======================================================================== */
  /* THE FOLLOWING CODE IS MANAGED BY SIMULACRUM; PLEASE DO NOT EDIT!!!!      */
  /* ======================================================================== */

  /**
    * Summon an instance of [[TraversablePathParts]] for `A`.
    */
  @inline def apply[A](implicit instance: TraversablePathParts[A]): TraversablePathParts[A] = instance

  object ops {
    implicit def toAllTraversablePathPartsOps[A](target: A)(implicit tc: TraversablePathParts[A]): AllOps[A] {
      type TypeClassType = TraversablePathParts[A]
    } =
      new AllOps[A] {
        type TypeClassType = TraversablePathParts[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  trait Ops[A] extends Serializable {
    type TypeClassType <: TraversablePathParts[A]
    def self: A
    val typeClassInstance: TypeClassType
    def toSeq: Seq[String] = typeClassInstance.toSeq(self)
    def toVector: Vector[String] = typeClassInstance.toVector(self)
  }
  trait AllOps[A] extends Ops[A]
  trait ToTraversablePathPartsOps extends Serializable {
    implicit def toTraversablePathPartsOps[A](target: A)(implicit tc: TraversablePathParts[A]): Ops[A] {
      type TypeClassType = TraversablePathParts[A]
    } =
      new Ops[A] {
        type TypeClassType = TraversablePathParts[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  object nonInheritedOps extends ToTraversablePathPartsOps

  /* ======================================================================== */
  /* END OF SIMULACRUM-MANAGED CODE                                           */
  /* ======================================================================== */

}
