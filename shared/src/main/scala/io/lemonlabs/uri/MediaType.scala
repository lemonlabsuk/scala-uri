package io.lemonlabs.uri

case class MediaType(rawValue: Option[String], parameters: Vector[(String, String)]) {

  /**
    * @return the value of this mediatype. Defaults to text/plain as per RFC2397 section 2
    */
  def value: String = rawValue.getOrElse("text/plain")

  /**
    * @return the charset for this mediatype. Defaults to US-ASCII as per RFC2397 section 2
    */
  def charset: String = rawCharset.getOrElse("US-ASCII")

  private def typeDelimiterIndex = value.indexOf('/')
  private def suffixDelimiterIndex = {
    val lastPlusIndex = value.lastIndexOf('+')
    if (lastPlusIndex > typeDelimiterIndex) lastPlusIndex
    else value.length
  }

  /**
    * @return The type for this mediatype. For example, will return `application`
    *         for the mediatype `application/ld+json`
    */
  def typ = typeDelimiterIndex match {
    case -1    => value
    case index => value.slice(0, index)
  }

  /**
    * @return The subtype for this mediatype. For example, will return `ld`
    *         for the mediatype `application/ld+json`. Returns empty string if there is no subtype.
    */
  def subTyp = typeDelimiterIndex match {
    case -1    => ""
    case index => value.slice(index + 1, suffixDelimiterIndex)
  }

  /**
    * @return The suffix for this mediatype. For example, will return `json`
    *         for the mediatype `application/ld+json`. Returns empty string if there is no suffix.
    */
  def suffix: String = {
    val index = math.min(suffixDelimiterIndex + 1, value.length)
    value.substring(index)
  }

  def rawCharset: Option[String] = parameters.collectFirst {
    case (k, v) if quotedStringEquals(k, "charset") => v
  }

  private def quotedStringEquals(s: String, matches: String): Boolean =
    s.equalsIgnoreCase(matches) || s.equalsIgnoreCase("\"" + matches + "\"")

  override def toString: String =
    rawValue.getOrElse("") + parameters.foldLeft("") { case (str, (key, v)) => s"$str;$key=$v" }
}
