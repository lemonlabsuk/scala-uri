package com.netaporter.uri.decoding
import PercentDecoder._


object PercentDecoder extends PercentDecoder(ignoreInvalidPercentEncoding = false) {
  protected  val errorMessage =
    "It looks like this URL isn't Percent Encoded. If so, you can use either" +
    "PercentDecoder(ignoreInvalidPercentEncoding=true) or NoopDecoder to suppress this Exception"

  protected val cs = "UTF-8"
}

case class PercentDecoder(ignoreInvalidPercentEncoding: Boolean) extends UriDecoder {
  def decode(s: String) = try {
    val segments = s.split('%')
    val decodedSegments = segments.tail.flatMap {
      case seg if seg.length > 1 =>
        val percentByte = Integer.parseInt(seg.substring(0, 2), 16).toByte
        percentByte +: seg.substring(2).getBytes(cs)

      case seg if ignoreInvalidPercentEncoding =>
        '%'.toByte +: seg.getBytes(cs)

      case seg =>
        throw new UriDecodeException(s"Encountered '%' followed by '$seg'. $errorMessage")
    }
    segments.head + new String(decodedSegments, cs)
  } catch {
    case e: NumberFormatException => throw new UriDecodeException(s"Encountered '%' followed by a non hex number. $errorMessage")
  }
}
