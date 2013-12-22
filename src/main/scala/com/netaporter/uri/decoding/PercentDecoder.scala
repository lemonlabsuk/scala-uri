package com.netaporter.uri.decoding


/**
 * Date: 23/06/2013
 * Time: 20:38
 */
object PercentDecoder extends UriDecoder {

  def decode(s: String) = try {
    val segments = s.split('%')
    val decodedSegments = segments.tail.map(seg => {
      val percentByte = Integer.parseInt(seg.substring(0, 2), 16).toByte
      new String(Array(percentByte), "UTF-8") + seg.substring(2)
    })
    segments.head + decodedSegments.mkString
  } catch {
    case e: NumberFormatException => throw new UriDecodeException("Encountered '%' followed by a non hex number. It looks like this URL isn't Percent Encoded. If so, look at using the NoopDecoder")
  }
}
