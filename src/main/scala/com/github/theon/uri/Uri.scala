package com.github.theon.uri

import com.github.theon.uri.Uri._
import com.github.theon.uri.Encoders.PercentEncoder
import com.github.theon.uri.Encoders.encode

case class Uri (
  protocol:Option[String],
  host:Option[String],
  port:Option[Int],
  pathParts:List[String],
  query:Querystring
) {

  def this(scheme:Option[String], host:Option[String], path:String, query:Querystring = Querystring()) = {
    this(scheme, host, None, path.dropWhile(_ == '/').split('/').toList, query)
  }

  /**
   * Adds a new Query String parameter key-value pair. If the value for the Query String parmeter is None, then this
   * Query String parameter will not be rendered in calls to toString or toStringRaw
   * @param kv Tuple2 representing the querystring parameter
   * @return A new Uri with the new Query String parameter
   */
  def param(kv:(String, Any)) = {
    val (k,v) = kv
    v match {
      case valueOpt:Option[_] =>
        copy(query = query & (k, valueOpt))
      case _ =>
        copy(query = query & (k, Some(v)))
    }
  }

  def scheme = protocol

  /**
   * Adds a new Query String parameter key-value pair. If the value for the Query String parmeter is None, then this
   * Query String parameter will not be rendered in calls to toString or toStringRaw
   * @param kv Tuple2 representing the querystring parameter
   * @return A new Uri with the new Query String parameter
   */
  def ?(kv:(String, Any)) = param(kv)

  /**
   * Adds a new Query String parameter key-value pair. If the value for the Query String parmeter is None, then this
   * Query String parameter will not be rendered in calls to toString or toStringRaw
   * @param kv Tuple2 representing the querystring parameter
   * @return A new Uri with the new Query String parameter
   */
  def &(kv:(String, Any)) = param(kv)

  /**
   * Returns the path with no encoding taking place (e.g. non ASCII characters will not be percent encoded)
   * @return String containing the raw path for this Uri
   */
  def pathRaw = path(None)

  /**
   * Returns the encoded path. By default non ASCII characters in the path are percent encoded.
   * @return String containing the path for this Uri
   */
  def path(implicit e:Enc = PercentEncoder):String = path(Some(e))

  protected def path(e:Option[Enc]):String = {
    "/" +
    pathParts.map(p => {
      if(e.isDefined) encode(p, e.get) else p
    }).mkString("/")
  }

  /**
   * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
   * with the specified value. If the value passed in is None, then all Query String parameters with the specified key
   * are removed
   *
   * @param k Key for the Query String parameter(s) to replace
   * @param v value to replace with
   * @return A new Uri with the result of the replace
   */
  def replaceParams(k:String, v:Any) = {
    v match {
      case valueOpt:Option[_] =>
        copy(query = query.replaceParams(k, valueOpt))
      case _ =>
        copy(query = query.replaceParams(k, Some(v)))
    }
  }

  /**
   * Removes all Query String parameters with the specified key
   * @param k Key for the Query String parameter(s) to remove
   * @return
   */
  def removeParams(k:String) = {
    copy(query = query.removeParams(k))
  }

  /**
   * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
   * with the specified value.
   *
   * @deprecated Use replaceParams() instead
   */
  @Deprecated
  def replace(k:String, v:String) = {
    copy(query = query.replaceParams(k, Some(v)))
  }

  override def toString = toString(PercentEncoder)
  def toString(implicit e:Enc = PercentEncoder):String = toString(Some(e))

  /**
   * Returns the string representation of this Uri with no encoding taking place
   * (e.g. non ASCII characters will not be percent encoded)
   * @return String containing this Uri in it's raw form
   */
  def toStringRaw():String = toString(None)

  protected def toString(e:Option[Enc]):String = {
    //If there is no scheme, we use protocol relative
    val schemeStr = scheme.map(_ + "://").getOrElse("//")
    host.map(schemeStr + _).getOrElse("") +
      port.map(":" + _).getOrElse("") +
      path(e) +
      query.toString("?", e)
  }
}

case class Querystring(params:Map[String,List[String]] = Map()) {

  /**
   * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
   * with the specified value.
   *
   * @deprecated Use replaceParams() instead
   */
  @Deprecated
  def replace(k:String, v:String) = {
    copy(params = params + (k -> List(v)))
  }

  /**
   * Replaces the all existing Query String parameters with the specified key with a single Query String parameter
   * with the specified value. If the value passed in is None, then all Query String parameters with the specified key
   * are removed
   *
   * @param k Key for the Query String parameter(s) to replace
   * @param v value to replace with
   * @return A new QueryString with the result of the replace
   */
  def replaceParams(k:String, v:Option[Any]) = {
    v match {
      case Some(v) => copy(params = params + (k -> List(v.toString)))
      case None => removeParams(k)
    }
  }

  /**
   * Removes all Query String parameters with the specified key
   * @param k Key for the Query String parameter(s) to remove
   * @return
   */
  def removeParams(k:String) = {
    copy(params = params.filterNot(_._1 == k))
  }

  /**
   * Adds a new Query String parameter key-value pair. If the value for the Query String parmeter is None, then this
   * Query String parameter will not be rendered in calls to toString or toStringRaw
   * @param kv Tuple2 representing the querystring parameter
   * @return A new Query String with the new Query String parameter
   */
  def &(kv:(String, Option[Any])) = {
    val (k,vOpt) = kv
    vOpt match {
      case Some(v) => {
        val values = params.getOrElse(k, List())
        copy(params = params + (k -> (v.toString :: values)))
      }
      case _ => this
    }
  }

  override def toString() = toString(PercentEncoder)
  def toString(e:Enc):String = toString(Some(e))

  /**
   * Returns the string representation of this QueryString with no encoding taking place
   * (e.g. non ASCII characters will not be percent encoded)
   * @return String containing this QueryString in it's raw form
   */
  def toStringRaw():String = toString(None)

  def toString(prefix:String, e:Option[Enc]):String = {
    if(params.isEmpty) {
      ""
    } else {
      prefix + toString(e)
    }
  }

  protected def toString(e:Option[Enc]) = {
    params.flatMap(kv => {
      val (k,v) = kv
      if(e.isDefined) {
        v.map(encode(k, e.get) + "=" + encode(_, e.get))
      } else {
        v.map(k + "=" + _)
      }
    }).mkString("&")
  }
}

object Uri {
  type Enc = UriEncoder

  implicit def stringToUri(s:String) = parseUri(s)
  implicit def uriToString(uri:Uri)(implicit e:UriEncoder=PercentEncoder):String = uri.toString(e)
  implicit def encoderToChainerEncoder(enc:UriEncoder) = ChainedUriEncoder(enc :: Nil)

  def parseUri(s:CharSequence) :Uri = parseUri(s.toString)
  def parseUri(s:String) :Uri = UriParser.parse(s)

  def apply(scheme:String, host:String, path:String):Uri =
    new Uri(Some(scheme), Some(host), path)

  def apply(scheme:String, host:String, path:String, query:Querystring):Uri =
    new Uri(Some(scheme), Some(host), path, query)

  def apply(scheme:Option[String], host:String, path:String):Uri =
    new Uri(scheme, Some(host), path)

  def apply(scheme:Option[String], host:String, path:String, query:Querystring):Uri =
    new Uri(scheme, Some(host), path, query)

  def apply(path:String):Uri =
    new Uri(None, None, path)

  def apply(path:String, query:Querystring):Uri =
    new Uri(None, None, path, query)
}
