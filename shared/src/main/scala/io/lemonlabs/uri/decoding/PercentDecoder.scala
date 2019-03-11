package io.lemonlabs.uri.decoding
import io.lemonlabs.uri.decoding.PercentDecoder._

import scala.annotation.tailrec


object PercentDecoder extends PercentDecoder(ignoreInvalidPercentEncoding = false) {
  protected val errorMessage =
    "It looks like this URL isn't Percent Encoded. If so, you can use either" +
    "PercentDecoder(ignoreInvalidPercentEncoding=true) or NoopDecoder to suppress this Exception"

  protected val cs = "UTF-8"
  protected val percentByte = '%'.toByte
}

case class PercentDecoder(ignoreInvalidPercentEncoding: Boolean) extends UriDecoder {

  def decode(s: String) = {

    def toHexByte(hex: String): Option[Byte] = try {
      if (hex.length != 2)
        None
      else
        Some(Integer.parseInt(hex, 16).toByte)
    } catch {
      case e: NumberFormatException => None
    }

    @tailrec
    def go(remaining: List[Char], result: Array[Byte]): Array[Byte] =
      remaining match {
        case Nil =>
          result
        case '%' :: xs =>
          val hex = xs.take(2).mkString
          toHexByte(hex) match {
            case Some(b) =>
              go(xs.drop(2), result :+ b)
            case None if ignoreInvalidPercentEncoding =>
              go(xs, result :+ percentByte)
            case _ =>
              throw new UriDecodeException(s"Encountered '%' followed by a non hex number '$hex'. $errorMessage")
          }
        case ch :: xs =>
          go(xs, result ++ ch.toString.getBytes(cs))
      }

    new String(go(s.toCharArray.toList, Array.empty), cs)
  }
}
