package io.lemonlabs.uri.typesafe

import scala.compiletime.{erasedValue, summonInline}
import scala.deriving.Mirror

trait TraversableParamsDeriving {
  inline def product[A](implicit m: Mirror.ProductOf[A]): TraversableParams[A] = {
    val elemInstances = summonAll[m.MirroredElemTypes]

    new TraversableParams[A] {
      override def toSeq(a: A): Seq[(String, Option[String])] =
        a.asInstanceOf[Product].productElementNames
          .zip(a.asInstanceOf[Product].productIterator)
          .zip(elemInstances)
          .flatMap {
            case ((name, field), tc : QueryValue[_]) => Seq((name, tc.asInstanceOf[QueryValue[Any]].queryValue(field)))
            case ((name, field), tc : TraversableParams[_]) => tc.asInstanceOf[TraversableParams[Any]].toSeq(field)
          }
          .toSeq
    }
  }

  inline def summonAll[T <: Tuple]: List[QueryValue[_] | TraversableParams[_]] =
    inline erasedValue[T] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => summonInline[TraversableParams[t] | QueryValue[t]] :: summonAll[ts]
    }
}
