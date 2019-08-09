package io.lemonlabs.uri.typesafe

import simulacrum.typeclass
import scala.language.implicitConversions

@typeclass trait PathPart[A] {
  def path(a: A): String
}

object PathPart extends PathPartInstances

trait PathPartInstances1 {
  implicit val stringPathPart: PathPart[String] = a => a
}

trait PathPartInstances extends PathPartInstances1 {
  implicit def optionPathPart[A : PathPart]: PathPart[Option[A]] = a => a.map(PathPart[A].path).getOrElse("")
}
