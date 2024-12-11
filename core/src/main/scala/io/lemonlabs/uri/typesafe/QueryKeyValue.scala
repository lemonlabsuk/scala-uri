package io.lemonlabs.uri.typesafe

import cats.Contravariant
import cats.syntax.contravariant._
import simulacrum.typeclass

import scala.language.implicitConversions
import scala.annotation.implicitNotFound

@implicitNotFound("Could not find an instance of QueryKey for ${A}")
@typeclass trait QueryKey[A] extends Serializable {
  def queryKey(a: A): String
}

object QueryKey extends QueryKeyInstances {
  /* ======================================================================== */
  /* THE FOLLOWING CODE IS MANAGED BY SIMULACRUM; PLEASE DO NOT EDIT!!!!      */
  /* ======================================================================== */

  /** Summon an instance of [[QueryKey]] for `A`.
    */
  @inline def apply[A](implicit instance: QueryKey[A]): QueryKey[A] = instance

  object ops {
    implicit def toAllQueryKeyOps[A](target: A)(implicit tc: QueryKey[A]): AllOps[A] {
      type TypeClassType = QueryKey[A]
    } =
      new AllOps[A] {
        type TypeClassType = QueryKey[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  trait Ops[A] extends Serializable {
    type TypeClassType <: QueryKey[A]
    def self: A
    val typeClassInstance: TypeClassType
    def queryKey: String = typeClassInstance.queryKey(self)
  }
  trait AllOps[A] extends Ops[A]
  trait ToQueryKeyOps extends Serializable {
    implicit def toQueryKeyOps[A](target: A)(implicit tc: QueryKey[A]): Ops[A] {
      type TypeClassType = QueryKey[A]
    } =
      new Ops[A] {
        type TypeClassType = QueryKey[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  object nonInheritedOps extends ToQueryKeyOps

  /* ======================================================================== */
  /* END OF SIMULACRUM-MANAGED CODE                                           */
  /* ======================================================================== */

}

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

@implicitNotFound("Could not find an instance of QueryValue for ${A}")
@typeclass trait QueryValue[-A] extends Serializable { self =>
  def queryValue(a: A): Option[String]
}

object QueryValue extends QueryValueInstances {
  def derive[A] = new Derivation[A](())

  class Derivation[A](private val dummy: Unit) extends AnyVal {
    def by[B](f: A => B)(implicit qv: QueryValue[B]): QueryValue[A] =
      a => qv.queryValue(f(a))
  }

  /* ======================================================================== */
  /* THE FOLLOWING CODE IS MANAGED BY SIMULACRUM; PLEASE DO NOT EDIT!!!!      */
  /* ======================================================================== */

  /** Summon an instance of [[QueryValue]] for `A`.
    */
  @inline def apply[A](implicit instance: QueryValue[A]): QueryValue[A] = instance

  object ops {
    implicit def toAllQueryValueOps[A](target: A)(implicit tc: QueryValue[A]): AllOps[A] {
      type TypeClassType = QueryValue[A]
    } =
      new AllOps[A] {
        type TypeClassType = QueryValue[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  trait Ops[A] extends Serializable {
    type TypeClassType <: QueryValue[A]
    def self: A
    val typeClassInstance: TypeClassType
    def queryValue: Option[String] = typeClassInstance.queryValue(self)
  }
  trait AllOps[A] extends Ops[A]
  trait ToQueryValueOps extends Serializable {
    implicit def toQueryValueOps[A](target: A)(implicit tc: QueryValue[A]): Ops[A] {
      type TypeClassType = QueryValue[A]
    } =
      new Ops[A] {
        type TypeClassType = QueryValue[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  object nonInheritedOps extends ToQueryValueOps

  /* ======================================================================== */
  /* END OF SIMULACRUM-MANAGED CODE                                           */
  /* ======================================================================== */

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

@implicitNotFound("Could not find an instance of QueryKeyValue for ${A}")
@typeclass trait QueryKeyValue[A] extends Serializable {
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

  /* ======================================================================== */
  /* THE FOLLOWING CODE IS MANAGED BY SIMULACRUM; PLEASE DO NOT EDIT!!!!      */
  /* ======================================================================== */

  /** Summon an instance of [[QueryKeyValue]] for `A`.
    */
  @inline def apply[A](implicit instance: QueryKeyValue[A]): QueryKeyValue[A] = instance

  object ops {
    implicit def toAllQueryKeyValueOps[A](target: A)(implicit tc: QueryKeyValue[A]): AllOps[A] {
      type TypeClassType = QueryKeyValue[A]
    } =
      new AllOps[A] {
        type TypeClassType = QueryKeyValue[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  trait Ops[A] extends Serializable {
    type TypeClassType <: QueryKeyValue[A]
    def self: A
    val typeClassInstance: TypeClassType
    def queryKey: String = typeClassInstance.queryKey(self)
    def queryValue: Option[String] = typeClassInstance.queryValue(self)
    def queryKeyValue: (String, Option[String]) = typeClassInstance.queryKeyValue(self)
  }
  trait AllOps[A] extends Ops[A]
  trait ToQueryKeyValueOps extends Serializable {
    implicit def toQueryKeyValueOps[A](target: A)(implicit tc: QueryKeyValue[A]): Ops[A] {
      type TypeClassType = QueryKeyValue[A]
    } =
      new Ops[A] {
        type TypeClassType = QueryKeyValue[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  object nonInheritedOps extends ToQueryKeyValueOps

  /* ======================================================================== */
  /* END OF SIMULACRUM-MANAGED CODE                                           */
  /* ======================================================================== */

}

sealed trait QueryKeyValueInstances {
  implicit def tuple2QueryKeyValue[K: QueryKey, V: QueryValue]: QueryKeyValue[(K, V)] =
    QueryKeyValue(_._1, _._2)
}

@implicitNotFound("Could not find an instance of TraversableParams for ${A}")
@typeclass trait TraversableParams[A] extends Serializable {
  def toSeq(a: A): Seq[(String, Option[String])]
  def toVector(a: A): Vector[(String, Option[String])] =
    toSeq(a).toVector
}

object TraversableParams extends TraversableParamsInstances with TraversableParamsDeriving {
  /* ======================================================================== */
  /* THE FOLLOWING CODE IS MANAGED BY SIMULACRUM; PLEASE DO NOT EDIT!!!!      */
  /* ======================================================================== */

  /** Summon an instance of [[TraversableParams]] for `A`.
    */
  @inline def apply[A](implicit instance: TraversableParams[A]): TraversableParams[A] = instance

  object ops {
    implicit def toAllTraversableParamsOps[A](target: A)(implicit tc: TraversableParams[A]): AllOps[A] {
      type TypeClassType = TraversableParams[A]
    } =
      new AllOps[A] {
        type TypeClassType = TraversableParams[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  trait Ops[A] extends Serializable {
    type TypeClassType <: TraversableParams[A]
    def self: A
    val typeClassInstance: TypeClassType
    def toSeq: Seq[(String, Option[String])] = typeClassInstance.toSeq(self)
    def toVector: Vector[(String, Option[String])] = typeClassInstance.toVector(self)
  }
  trait AllOps[A] extends Ops[A]
  trait ToTraversableParamsOps extends Serializable {
    implicit def toTraversableParamsOps[A](target: A)(implicit tc: TraversableParams[A]): Ops[A] {
      type TypeClassType = TraversableParams[A]
    } =
      new Ops[A] {
        type TypeClassType = TraversableParams[A]
        val self: A = target
        val typeClassInstance: TypeClassType = tc
      }
  }
  object nonInheritedOps extends ToTraversableParamsOps

  /* ======================================================================== */
  /* END OF SIMULACRUM-MANAGED CODE                                           */
  /* ======================================================================== */

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
