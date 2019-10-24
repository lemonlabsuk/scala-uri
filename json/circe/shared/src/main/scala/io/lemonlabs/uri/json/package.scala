package io.lemonlabs.uri

package object json {
  implicit val circeSupport: JsonSupport = CirceSupport
}
