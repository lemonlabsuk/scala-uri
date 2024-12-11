package io.lemonlabs.uri.typesafe

import cats.syntax.contravariant._
import shapeless._
import shapeless.labelled._
import shapeless.ops.coproduct.Reify
import shapeless.ops.hlist.ToList

trait TraversableParamsDeriving {
  implicit def field[K <: Symbol, V](implicit K: Witness.Aux[K], V: QueryValue[V]): TraversableParams[FieldType[K, V]] =
    (a: FieldType[K, V]) => List(K.value.name -> V.queryValue(a))

  implicit def sub[K <: Symbol, V](implicit
      K: Witness.Aux[K],
      V: TraversableParams[V]
  ): TraversableParams[FieldType[K, V]] =
    (a: FieldType[K, V]) => V.toSeq(a)

  implicit val hnil: TraversableParams[HNil] =
    (_: HNil) => List.empty

  implicit def hcons[H, T <: HList](implicit
      H: TraversableParams[H],
      T: TraversableParams[T]
  ): TraversableParams[H :: T] =
    (a: H :: T) => H.toSeq(a.head) ++ T.toSeq(a.tail)

  def product[A, R <: HList](implicit gen: LabelledGeneric.Aux[A, R], R: TraversableParams[R]): TraversableParams[A] =
    (a: A) => R.toSeq(gen.to(a))
}
