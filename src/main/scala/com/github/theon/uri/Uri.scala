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
    this(scheme, host, None, path.split('/').toList, query)
  }

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

  def ?(kv:(String, Any)) = param(kv)
  def &(kv:(String, Any)) = param(kv)

  def pathRaw = path(None)
  def path(implicit e:Enc = PercentEncoder):String = path(Some(e))

  def path(e:Option[Enc]):String = {
    pathParts.map(p => {
      if(e.isDefined) encode(p, e.get) else p
    }).mkString("/")
  }

  def replaceParams(k:String, v:Any) = {
    v match {
      case valueOpt:Option[_] =>
        copy(query = query.replaceParams(k, valueOpt))
      case _ =>
        copy(query = query.replaceParams(k, Some(v)))
    }
  }

  def removeParams(k:String) = {
    copy(query = query.removeParams(k))
  }

  /**
   * @deprecated Use replaceParams() instead
   */
  @Deprecated
  def replace(k:String, v:String) = {
    copy(query = query.replaceParams(k, Some(v)))
  }

  override def toString = toString(PercentEncoder)
  def toString(implicit e:Enc = PercentEncoder):String = toString(Some(e))
  def toStringRaw():String = toString(None)

  def toString(e:Option[Enc]):String = {
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
   * @deprecated Use replaceParams() instead
   */
  @Deprecated
  def replace(k:String, v:String) = {
    copy(params = params + (k -> List(v)))
  }

  def replaceParams(k:String, v:Option[Any]) = {
    v match {
      case Some(v) => copy(params = params + (k -> List(v.toString)))
      case None => removeParams(k)
    }
  }

  def removeParams(k:String) = {
    copy(params = params.filterNot(_._1 == k))
  }

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
  def toStringRaw():String = toString(None)

  def toString(prefix:String, e:Option[Enc]):String = {
    if(params.isEmpty) {
      ""
    } else {
      prefix + toString(e)
    }
  }

  def toString(e:Option[Enc]) = {
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
  //TODO: uncomment when 2.9.2 support is dropped
  //import scala.language.implicitConversions

  type Enc = UriEncoder

  implicit def stringToUri(s:String) = parseUri(s)
  implicit def uriToString(uri:Uri)(implicit e:UriEncoder=PercentEncoder):String = uri.toString(e)
  implicit def encoderToChainerEncoder(enc:UriEncoder) = ChainedUriEncoder(enc :: Nil)

  def parseUri(s:String) = UriParser.parse(s)

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
