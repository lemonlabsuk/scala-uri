package io.lemonlabs.uri.typesafe

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.{RelativeUrl, Url}

package object dsl {

  import PathPart.ops._

  import scala.language.implicitConversions

  implicit def stringToUri(s: String)(implicit c: UriConfig = UriConfig.default): Url = Url.parse(s)(c)

  implicit def stringToUriDsl(s: String)(implicit c: UriConfig = UriConfig.default): TypesafeUrlDsl = new TypesafeUrlDsl(stringToUri(s)(c))

  implicit def urlToUrlDsl(uri: Url): TypesafeUrlDsl = new TypesafeUrlDsl(uri)

  implicit def pathPartToUrlDsl[A: PathPart](a: A): TypesafeUrlDsl = new TypesafeUrlDsl(a.path)

  implicit def queryParamToUriDsl[A](a: A)(implicit c: UriConfig = UriConfig.default, tc: QueryKeyValue[A]): TypesafeUrlDsl =
    new TypesafeUrlDsl(RelativeUrl.empty.addParam(tc.queryKey(a), tc.queryValue(a)))
}
