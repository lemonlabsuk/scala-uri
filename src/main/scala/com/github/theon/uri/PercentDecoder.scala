package com.github.theon.uri


/**
 * Date: 23/06/2013
 * Time: 20:38
 */
object PercentDecoder extends UriDecoder {

  def decode(u: Uri) = u.copy (
    pathParts = u.pathParts.map(decodeString),
    query = u.query.copy (
      u.query.params.map(kv => decodeString(kv._1) -> kv._2.map(decodeString))
    )
  )

  protected def decodeString(s: String) = {
    val segments = s.split('%')
    val decodedSegments = segments.tail.map(seg => {
      val percentByte = Integer.parseInt(seg.substring(0, 2), 16).toByte
      new String(Array(percentByte), "UTF-8") + seg.substring(2)
    })
    segments.head + decodedSegments.mkString
  }
}

object NoopDecoder extends UriDecoder {
  def decode(u: Uri) = u
}

trait UriDecoder {
  def decode(s: Uri): Uri
}