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

  def this(protocol:Option[String], host:Option[String], path:String, query:Querystring = Querystring()) = {
    this(protocol, host, None, path.split('/').toList, query)
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

  def ?(kv:(String, Any)) = param(kv)
  def &(kv:(String, Any)) = param(kv)

  def pathRaw = path(None)
  def path(implicit e:Enc = PercentEncoder):String = path(Some(e))

  def path(e:Option[Enc]):String = {
    pathParts.map(p => {
      if(e.isDefined) encode(p, e.get) else p
    }).mkString("/")
  }

  def replace(k:String, v:String) = {
    copy(query = query.replace(k, v))
  }

  def toString(implicit e:Enc = PercentEncoder):String = toString(Some(e))
  def toStringRaw():String = toString(None)

  def toString(e:Option[Enc]):String = {
    protocol.map(_ + "://").getOrElse("") +
    host.getOrElse("") +
    path(e) +
    query.toString("?", e)
  }
}

case class Querystring(params:Map[String,List[String]] = Map()) {

  def replace(k:String, v:String) = {
    copy(params = params + (k -> List(v)))
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

  def toString(e:Enc = PercentEncoder):String = toString(Some(e))
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

  type Enc = UriEncoder

  implicit def stringToUri(s:String) = parseUri(s)
  implicit def uriToString(uri:Uri)(implicit e:UriEncoder):String = uri.toString
  implicit def encoderToChainerEncoder(enc:UriEncoder) = ChainedUriEncoder(enc :: Nil)

  def parseUri(s:String) = UriParser.parse(s)

  def apply(protocol:String, host:String, path:String):Uri =
    new Uri(Some(protocol), Some(host), path)

  def apply(protocol:String, host:String, path:String, query:Querystring):Uri =
    new Uri(Some(protocol), Some(host), path, query)

  def apply(path:String):Uri =
    new Uri(None, None, path)

  def apply(path:String, query:Querystring):Uri =
    new Uri(None, None, path, query)
}
