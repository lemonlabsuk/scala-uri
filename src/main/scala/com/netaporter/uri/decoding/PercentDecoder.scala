package com.netaporter.uri.decoding


/**
 * Date: 23/06/2013
 * Time: 20:38
 */
object PercentDecoder extends UriDecoder {

  def decode(s: String) = try {
    val segments = s.split('%')
    val decodedSegments = segments.tail.flatMap(seg => {
      val percentByte = Integer.parseInt(seg.substring(0, 2), 16).toByte
      percentByte +: seg.substring(2).getBytes("UTF-8")
    })
    segments.head + new String(decodedSegments, "UTF-8")
  } catch {
    case e: NumberFormatException => throw new UriDecodeException("Encountered '%' followed by a non hex number. It looks like this URL isn't Percent Encoded. If so, look at using the NoopDecoder")
  }
}
