package io.lemonlabs.uri.decoding

import scala.annotation.tailrec
import scala.collection.mutable

object PercentDecoder extends PercentDecoder(ignoreInvalidPercentEncoding = false) {
  protected val errorMessage =
    "It looks like this URL isn't Percent Encoded. Ideally you should Percent Encode the relevant parts " +
      "of your URL before passing to scala-uri. Alternatively, you can use a UriConfig with either " +
      "PercentDecoder(ignoreInvalidPercentEncoding=true) or NoopDecoder to suppress this Exception"

  protected val cs = "UTF-8"
  protected val percentByte = '%'.toByte
}

case class PercentDecoder(ignoreInvalidPercentEncoding: Boolean) extends UriDecoder {
  import io.lemonlabs.uri.decoding.PercentDecoder._

  def decode(s: String): String = {
    def toHexByte(hex: String): Option[Byte] =
      try {
        if (hex.length != 2)
          None
        else
          Some(Integer.parseInt(hex, 16).toByte)
      } catch {
        case e: NumberFormatException => None
      }

    @tailrec
    def go(remaining: List[Char], result: mutable.ArrayBuilder[Byte]): Array[Byte] =
      remaining match {
        case Nil =>
          result.result()
        case '%' :: xs =>
          val hex = xs.take(2).mkString
          toHexByte(hex) match {
            case Some(b) =>
              go(xs.drop(2), result += b)
            case None if ignoreInvalidPercentEncoding =>
              go(xs, result += percentByte)
            case _ =>
              throw new UriDecodeException(s"Encountered '%' followed by a non hex number '$hex'. $errorMessage")
          }
        case ch :: xs =>
          go(xs, result ++= ch.toString.getBytes(cs))
      }

    new String(go(s.toCharArray.toList, new mutable.ArrayBuilder.ofByte), cs)
  }
}
