package io.lemonlabs.uri

package object json {
  implicit val sprayJsonSupport: JsonSupport = SprayJsonSupport
}
