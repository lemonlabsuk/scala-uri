package io.lemonlabs.uri.typesafe

import scala.compiletime.{erasedValue, summonInline}
import scala.deriving.Mirror

private[typesafe] class TraversablePathPartsDerived[A](elemInstances: List[TraversablePathParts[_]])
    extends TraversablePathParts[A]:
  override def toSeq(a: A): Seq[String] =
    a.asInstanceOf[Product]
      .productIterator
      .zip(elemInstances)
      .flatMap { case (field, tc) => tc.asInstanceOf[TraversablePathParts[Any]].toSeq(field) }
      .toSeq

trait TraversablePathPartsDeriving {
  inline def product[A](implicit m: Mirror.ProductOf[A]): TraversablePathParts[A] = {
    val elemInstances = summonAll[m.MirroredElemTypes]

    TraversablePathPartsDerived(elemInstances)
  }

  inline def summonAll[T <: Tuple]: List[TraversablePathParts[_]] =
    inline erasedValue[T] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => summonInline[TraversablePathParts[t]] :: summonAll[ts]
    }
}
