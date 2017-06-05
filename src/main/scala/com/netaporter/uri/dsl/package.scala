package com.netaporter.uri

import com.netaporter.uri.config.UriConfig

package object dsl {

  import scala.language.implicitConversions

  implicit def uriToUriOps(uri: Uri): UriDsl = new UriDsl(uri)

  implicit def stringToUri(s: String)(implicit c: UriConfig = UriConfig.default): Uri = Uri.parse(s)(c)
  implicit def stringToUriDsl(s: String)(implicit c: UriConfig = UriConfig.default): UriDsl = new UriDsl(stringToUri(s)(c))

  implicit def queryParamToUriDsl(kv: (String, Any))(implicit c: UriConfig = UriConfig.default): UriDsl = new UriDsl(Uri.empty.addParam(kv._1, kv._2))

  implicit def uriToString(uri: Uri)(implicit c: UriConfig = UriConfig.default): String = uri.toString(c)
}
