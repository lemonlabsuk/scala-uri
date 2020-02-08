package io.lemonlabs.uri.typesafe

import cats.Contravariant
import cats.syntax.contravariant._
import shapeless._
import shapeless.labelled._
import shapeless.ops.coproduct.Reify
import shapeless.ops.hlist.ToList
import simulacrum.typeclass

import scala.language.implicitConversions

@typeclass trait QueryKey[A] {
  def queryKey(a: A): String
}

object QueryKey extends QueryKeyInstances

sealed trait QueryKeyInstances1 {
  implicit val contravariant: Contravariant[QueryKey] = new Contravariant[QueryKey] {
    def contramap[A, B](fa: QueryKey[A])(f: B => A): QueryKey[B] = (b: B) => fa.queryKey(f(b))
  }
}

sealed trait QueryKeyInstances extends QueryKeyInstances1 {
  implicit val stringQueryKey: QueryKey[String] = a => a
  implicit final val booleanQueryValue: QueryKey[Boolean] = stringQueryKey.contramap(_.toString)
  implicit final val charQueryValue: QueryKey[Char] = stringQueryKey.contramap(_.toString)
  implicit final val intQueryValue: QueryKey[Int] = stringQueryKey.contramap(_.toString)
  implicit final val longQueryValue: QueryKey[Long] = stringQueryKey.contramap(_.toString)
  implicit final val floatQueryValue: QueryKey[Float] = stringQueryKey.contramap(_.toString)
  implicit final val doubleQueryValue: QueryKey[Double] = stringQueryKey.contramap(_.toString)
  implicit final val uuidQueryValue: QueryKey[java.util.UUID] = stringQueryKey.contramap(_.toString)
}

@typeclass trait QueryValue[-A] { self =>
  def queryValue(a: A): Option[String]
}

object QueryValue extends QueryValueInstances {
  def derive[A]: Derivation[A] = new Derivation[A](())

  class Derivation[A](private val dummy: Unit) extends AnyVal {
    def by[C <: Coproduct, R <: HList](
        key: A => String
    )(implicit gen: Generic.Aux[A, C], reify: Reify.Aux[C, R], toList: ToList[R, A]): QueryValue[A] =
      a => toList(reify()).iterator.map(x => x -> key(x)).toMap.get(a)
  }
}

sealed trait QueryValueInstances2 {
  implicit val contravariant: Contravariant[QueryValue] = new Contravariant[QueryValue] {
    def contramap[A, B](fa: QueryValue[A])(f: B => A): QueryValue[B] = (b: B) => fa.queryValue(f(b))
  }
}

sealed trait QueryValueInstances1 extends QueryValueInstances2 {
  implicit final val stringQueryValue: QueryValue[String] = Option(_)
  implicit final val booleanQueryValue: QueryValue[Boolean] = stringQueryValue.contramap(_.toString)
  implicit final val charQueryValue: QueryValue[Char] = stringQueryValue.contramap(_.toString)
  implicit final val intQueryValue: QueryValue[Int] = stringQueryValue.contramap(_.toString)
  implicit final val longQueryValue: QueryValue[Long] = stringQueryValue.contramap(_.toString)
  implicit final val floatQueryValue: QueryValue[Float] = stringQueryValue.contramap(_.toString)
  implicit final val doubleQueryValue: QueryValue[Double] = stringQueryValue.contramap(_.toString)
  implicit final val uuidQueryValue: QueryValue[java.util.UUID] = stringQueryValue.contramap(_.toString)
  implicit final val noneQueryValue: QueryValue[None.type] = _ => None
}

sealed trait QueryValueInstances extends QueryValueInstances1 {
  implicit final def optionQueryValue[A: QueryValue]: QueryValue[Option[A]] = _.flatMap(QueryValue[A].queryValue)
}

@typeclass trait QueryKeyValue[A] {
  def queryKey(a: A): String

  def queryValue(a: A): Option[String]

  def queryKeyValue(a: A): (String, Option[String]) =
    queryKey(a) -> queryValue(a)
}

object QueryKeyValue extends QueryKeyValueInstances {
  def apply[T, K: QueryKey, V: QueryValue](toKey: T => K, toValue: T => V): QueryKeyValue[T] =
    new QueryKeyValue[T] {
      def queryKey(a: T): String = QueryKey[K].queryKey(toKey(a))
      def queryValue(a: T): Option[String] = QueryValue[V].queryValue(toValue(a))
    }
}

sealed trait QueryKeyValueInstances {
  implicit def tuple2QueryKeyValue[K: QueryKey, V: QueryValue]: QueryKeyValue[(K, V)] =
    QueryKeyValue(_._1, _._2)
}

@typeclass trait TraversableParams[A] {
  def toSeq(a: A): Seq[(String, Option[String])]
  def toVector(a: A): Vector[(String, Option[String])] =
    toSeq(a).toVector
}

object TraversableParams extends TraversableParamsInstances {
  implicit def field[K <: Symbol, V](implicit K: Witness.Aux[K], V: QueryValue[V]): TraversableParams[FieldType[K, V]] =
    (a: FieldType[K, V]) => List(K.value.name -> V.queryValue(a))

  implicit def sub[K <: Symbol, V](implicit K: Witness.Aux[K],
                                   V: TraversableParams[V]): TraversableParams[FieldType[K, V]] =
    (a: FieldType[K, V]) => V.toSeq(a)

  implicit val hnil: TraversableParams[HNil] =
    (_: HNil) => List.empty

  implicit def hcons[H, T <: HList](implicit H: TraversableParams[H],
                                    T: TraversableParams[T]): TraversableParams[H :: T] =
    (a: H :: T) => H.toSeq(a.head) ++ T.toSeq(a.tail)

  def product[A, R <: HList](implicit gen: LabelledGeneric.Aux[A, R], R: TraversableParams[R]): TraversableParams[A] =
    (a: A) => R.toSeq(gen.to(a))
}

sealed trait TraversableParamsInstances1 {
  implicit val contravariant: Contravariant[TraversableParams] = new Contravariant[TraversableParams] {
    override def contramap[A, B](fa: TraversableParams[A])(f: B => A): TraversableParams[B] = b => fa.toSeq(f(b))
  }
}

sealed trait TraversableParamsInstances extends TraversableParamsInstances1 {
  implicit def iterableTraversableParams[A](implicit tc: QueryKeyValue[A]): TraversableParams[Iterable[A]] =
    (ax: Iterable[A]) => ax.map((a: A) => tc.queryKey(a) -> tc.queryValue(a)).toSeq

  implicit def seqTraversableParams[A](implicit tc: QueryKeyValue[A]): TraversableParams[Seq[A]] =
    (ax: Seq[A]) => ax.map((a: A) => tc.queryKey(a) -> tc.queryValue(a))

  implicit def listTraversableParams[A](implicit tc: QueryKeyValue[A]): TraversableParams[List[A]] =
    (ax: List[A]) => ax.map((a: A) => tc.queryKey(a) -> tc.queryValue(a))

  implicit def singleTraversableParams[A](implicit tc: QueryKeyValue[A]): TraversableParams[A] =
    (a: A) => Seq(tc.queryKey(a) -> tc.queryValue(a))

  implicit def vectorTraversableParams[A](implicit tc: QueryKeyValue[A]): TraversableParams[Vector[A]] =
    (ax: Vector[A]) => ax.map((a: A) => tc.queryKey(a) -> tc.queryValue(a))

  implicit def mapTraversableParams[K, V](implicit tck: QueryKey[K], tcv: QueryValue[V]): TraversableParams[Map[K, V]] =
    (ax: Map[K, V]) => ax.map { case (k, v) => tck.queryKey(k) -> tcv.queryValue(v) }.toSeq
}
