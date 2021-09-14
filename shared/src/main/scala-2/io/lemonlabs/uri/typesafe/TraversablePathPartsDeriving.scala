package io.lemonlabs.uri.typesafe

import cats.syntax.contravariant._
import shapeless._
import shapeless.labelled._

trait TraversablePathPartsDeriving {
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
}
