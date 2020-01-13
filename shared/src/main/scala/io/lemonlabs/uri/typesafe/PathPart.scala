package io.lemonlabs.uri.typesafe

import java.util.UUID

import cats.Contravariant
import cats.syntax.contravariant._
import simulacrum.typeclass

import scala.language.implicitConversions

@typeclass trait PathPart[A] {
  def path(a: A): String
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
