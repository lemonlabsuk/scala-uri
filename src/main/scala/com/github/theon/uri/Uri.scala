package com.github.theon.uri

import java.net.URI
import com.github.theon.uri.Uri._

case class Uri(protocol:Option[String], host:Option[String], path:String, query:Querystring = Querystring()) {

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

  def replace(k:String, v:String) = {
    copy(query = query.replace(k, v))
  }

  override def toString() = toString(true)
  def toStringRaw() = toString(false)

  def toString(enc:Boolean) = {
    val encPath = if(enc) uriEncode(path) else path

    protocol.map(_ + "://").getOrElse("") +
    host.getOrElse("") +
    encPath +
    query.toString("?", enc)
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

  override def toString() = toString(true)
  def toStringRaw() = toString(false)

  def toString(prefix:String, enc:Boolean):String = {
    if(params.isEmpty) {
      ""
    } else {
      prefix + toString(enc)
    }
  }

  def toString(enc:Boolean) = {
    params.flatMap(kv => {
      val (k,v) = kv
      if(enc) {
        v.map(uriEncode(k) + "=" + uriEncode(_))
      } else {
        v.map(k + "=" + _)
      }
    }).mkString("&")
  }
}

object Uri {
  implicit def stringToUri(s:String) = parseUri(s)
  implicit def uriToString(uri:Uri):String = uri.toString

  def parseUri(s:String) = {
    val uri = new URI(s)
    val q = parseQuery(Option(uri.getQuery))
    Uri(Option(uri.getScheme), Option(uri.getAuthority), uri.getPath, q)
  }

  def parseQuery(qsOpt:Option[String]) = {
    qsOpt match {
      case None => Querystring()
      case Some(qs) => {
        val tuples = qs.split("&").map(pairStr => {
          val pair = pairStr.split("=")
          (pair(0), pair(1))
        }).toList

        val map = tuples.groupBy(_._1).map(kv => {
          val (k,v) = kv
          (k,v.map(_._2))
        })

        Querystring(map)
      }
    }
  }

  def uriEncode(s:String) = new URI(null, s, null).toASCIIString
  def uriDecode(s:String) = URI.create(s).getPath

  def apply(protocol:String, host:String, path:String):Uri =
    Uri(Some(protocol), Some(host), path)

  def apply(protocol:String, host:String, path:String, query:Querystring):Uri =
    Uri(Some(protocol), Some(host), path, query)

  def apply(path:String):Uri =
    Uri(None, None, path)

  def apply(path:String, query:Querystring):Uri =
    Uri(None, None, path, query)
}
