package io.lemonlabs.uri

case class MediaType(rawValue: Option[String], parameters: Vector[(String,String)]) {

  /**
    * @return the value of this mediatype. Defaults to text/plain as per RFC2397 section 2
    */
  def value: String = rawValue.getOrElse("text/plain")

  /**
    * @return the charset for this mediatype. Defaults to US-ASCII as per RFC2397 section 2
    */
  def charset: String = rawCharset.getOrElse("US-ASCII")


  private def typeDelimiterIndex = value.indexOf('/')

  def typ = typeDelimiterIndex match {
    case -1 => value
    case index  => value.slice(0, index)
  }

  def subTyp = typeDelimiterIndex match {
    case -1 => ""
    case index => value.substring(index + 1)
  }

  def rawCharset: Option[String] = parameters.collectFirst {
    case (k,v) if k.equalsIgnoreCase("charset") => v
  }

  override def toString: String =
    rawValue.getOrElse("") + parameters.foldLeft("") { case (str, (key, v)) => s"$str;$key=$v" }
}
