package io.lemonlabs.uri.typesafe

import java.util.UUID

import cats.Contravariant
import cats.syntax.contravariant._
import shapeless._
import shapeless.labelled._
import simulacrum.typeclass

import scala.language.implicitConversions

@typeclass trait PathPart[-A] {
  def path(a: A): String
  def splitPath(a: A): Seq[String] = path(a).split('/').toSeq
}

object PathPart extends PathPartInstances

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

@typeclass trait TraversablePathParts[A] {
  def toSeq(a: A): Seq[String]
  def toVector(a: A): Vector[String] =
    toSeq(a).toVector
}

object TraversablePathParts extends TraversablePathPartsInstances {
  implicit def field[K <: Symbol, V](implicit K: Witness.Aux[K],
                                     V: PathPart[V]): TraversablePathParts[FieldType[K, V]] =
    (a: FieldType[K, V]) => V.splitPath(a)

  implicit def sub[K <: Symbol, V](implicit K: Witness.Aux[K],
                                   V: TraversablePathParts[V]): TraversablePathParts[FieldType[K, V]] =
    (a: FieldType[K, V]) => V.toSeq(a)

  implicit val hnil: TraversablePathParts[HNil] =
    (_: HNil) => Seq.empty

  implicit def hcons[H, T <: HList](implicit H: TraversablePathParts[H],
                                    T: TraversablePathParts[T]): TraversablePathParts[H :: T] =
    (a: H :: T) => H.toSeq(a.head) ++ T.toSeq(a.tail)

  def product[A, R <: HList](implicit gen: Generic.Aux[A, R], R: TraversablePathParts[R]): TraversablePathParts[A] =
    (a: A) => R.toSeq(gen.to(a))
}
