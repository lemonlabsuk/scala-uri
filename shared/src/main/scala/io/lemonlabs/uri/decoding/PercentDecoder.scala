package io.lemonlabs.uri.decoding
import PercentDecoder._

import scala.collection.mutable.ArrayBuffer
import scala.util.Try


object PercentDecoder extends PercentDecoder(ignoreInvalidPercentEncoding = false) {
  protected val errorMessage =
    "It looks like this URL isn't Percent Encoded. If so, you can use either" +
    "PercentDecoder(ignoreInvalidPercentEncoding=true) or NoopDecoder to suppress this Exception"

  protected val cs = "UTF-8"
}

case class PercentDecoder(ignoreInvalidPercentEncoding: Boolean) extends UriDecoder {

  def decode(s: String) = {
    def charAt(str: String, index: Int) = Try(str.charAt(index)).toOption

    def substring(str: String,  beginIndex: Int, endIndex: Int) =
      Try(str.substring(beginIndex, endIndex)).toOption

    @scala.annotation.tailrec
    def go(index: Int, result: ArrayBuffer[Byte]): Array[Byte] = (charAt(s, index), substring(s, index + 1, index + 3)) match {
      case (None, _) => result.toArray
      case (Some('%'), Some(hex)) =>
        val (increment, percentByte) = Try(Integer.parseInt(hex, 16).toByte)
          .map { percentByte =>
            (3, percentByte)
          }.recover { case _ if ignoreInvalidPercentEncoding =>
            (1, '%'.toByte)
          }.getOrElse(throw new UriDecodeException(s"Encountered '%' followed by a non hex number. $errorMessage"))
        go(index + increment, result :+ percentByte)
      case (Some('%'), None) if !ignoreInvalidPercentEncoding =>
        val c = charAt(s, index + 1).getOrElse("")
        throw new UriDecodeException(s"Encountered '%' followed by '$c'. $errorMessage")
      case (Some(c), _) =>
        go(index + 1, result :+ c.toByte)
    }
    new String(go(0, ArrayBuffer.empty), cs)
  }
}
