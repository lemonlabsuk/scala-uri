package io.lemonlabs.uri

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import io.lemonlabs.uri.typesafe.QueryKeyValue

class TransformTests extends AnyWordSpec with Matchers {
  "mapQuery" should {
    "transform query params" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")
      val uri2 = uri.mapQuery { case (k, v) =>
        (k, v map (_ + "TEST"))
      }
      uri2.toString should equal("/test?param_1=helloTEST&param_2=goodbyeTEST&param_3=falseTEST")
    }

    "transform query params with a QueryKeyValue type class instance" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")

      case class UppercaseParam(key: String, value: Option[String])
      object UppercaseParam {
        implicit val queryKeyValue: QueryKeyValue[UppercaseParam] =
          QueryKeyValue(_.key.toUpperCase, _.value.map(_.toUpperCase))
      }

      val uri2 = uri.mapQuery { case (k, v) =>
        UppercaseParam(k, v)
      }
      uri2.toString should equal("/test?PARAM_1=HELLO&PARAM_2=GOODBYE&PARAM_3=FALSE")
    }

    "transform query param names" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")
      val uri2 = uri.mapQueryNames(_.split("_")(1))
      uri2.toString should equal("/test?1=hello&2=goodbye&3=false")
    }

    "flip query params" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")
      val uri2 = uri.mapQuery {
        case (k, Some(v)) => v -> Some(k)
        case o            => o
      }
      uri2.toString should equal("/test?hello=param_1&goodbye=param_2&false=param_3")
    }

    "transform query param values" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")
      val uri2 = uri.mapQueryValues(_.charAt(0).toString)
      uri2.toString should equal("/test?param_1=h&param_2=g&param_3=f")
    }
  }

  "filterQuery" should {
    "filter query params" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")
      val uri2 = uri.filterQuery {
        case (k, Some(v)) => (k + v).length > 13
        case (k, None)    => k.length > 13
      }
      uri2.toString should equal("/test?param_2=goodbye")
    }

    "filter out all query params" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")
      val uri2 = uri.filterQuery(_ => false)
      uri2.toString should equal("/test")
    }

    "filter query param names" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")
      val uri2 = uri.filterQueryNames(_ == "param_1")
      uri2.toString should equal("/test?param_1=hello")
    }

    "filter query param values" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")
      val uri2 = uri.filterQueryValues(_ == "false")
      uri2.toString should equal("/test?param_3=false")
    }

    "filter query param option values" in {
      val query = QueryString.parse("param_1=hello&param_2=goodbye&param_3=false&param_4")
      val query2 = query.filterOptionValues(!_.contains("false"))
      query2.toString should equal("param_1=hello&param_2=goodbye&param_4")
    }
  }

  "collectQuery" should {
    "transform query params" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")
      val uri2 = uri.collectQuery { case ("param_1", _) =>
        "param_1" -> "world"
      }
      uri2.toString should equal("/test?param_1=world")
    }

    "transform query params with a QueryKeyValue type class instance" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")

      case class UppercaseParam(key: String, value: Option[String])
      object UppercaseParam {
        implicit val queryKeyValue: QueryKeyValue[UppercaseParam] =
          QueryKeyValue(_.key.toUpperCase, _.value.map(_.toUpperCase))
      }

      val uri2 = uri.collectQuery {
        case (k, v) if k == "param_2" => UppercaseParam(k, v)
      }
      uri2.toString should equal("/test?PARAM_2=GOODBYE")
    }

    "transform query param names" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")
      val uri2 = uri.collectQuery { case ("param_1", v) =>
        "greeting" -> v
      }
      uri2.toString should equal("/test?greeting=hello")
    }

    "flip query params" in {
      val uri = Url.parse("/test?param_1=hello&param_2&param_3=false")
      val uri2 = uri.collectQuery { case (k, Some(v)) =>
        v -> Some(k)
      }
      uri2.toString should equal("/test?hello=param_1&false=param_3")
    }

    "transform query param values" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")
      val uri2 = uri.collectQuery { case (k, Some("hello")) =>
        k -> "hi"
      }
      uri2.toString should equal("/test?param_1=hi")
    }
  }

  "flatMapQuery" should {
    "transform to List of query params" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye")
      val uri2 = uri.flatMapQuery { case (k, v) =>
        List.fill(2)(k -> v)
      }
      uri2.toString should equal("/test?param_1=hello&param_1=hello&param_2=goodbye&param_2=goodbye")
    }
  }

  "mapUser" should {
    "not change a relative URL" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye")
      uri.mapUser(_ + "2") should equal(uri)
    }
    "not change a URL without authority" in {
      val uri = Url.parse("mailto:me@example.com")
      uri.mapUser(_ + "2") should equal(uri)
    }
    "change the user in an absolute URL" in {
      val uri = Url.parse("http://me@example.com/test").mapUser(_ + "2")
      uri.user should equal(Some("me2"))
      uri.toString should equal("http://me2@example.com/test")
    }
    "not change an absolute URL with no user-info" in {
      val uri = Url.parse("http://example.com/test")
      uri.mapUser(_ + "2") should equal(uri)
    }
  }

  "mapPassword" should {
    "not change a relative URL" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye")
      uri.mapPassword(_ + "2") should equal(uri)
    }
    "not change a URL without authority" in {
      val uri = Url.parse("mailto:me@example.com")
      uri.mapPassword(_ + "2") should equal(uri)
    }
    "change the password in an absolute URL" in {
      val uri = Url.parse("http://me:password@example.com/test").mapPassword(_ + "2")
      uri.password should equal(Some("password2"))
      uri.toString should equal("http://me:password2@example.com/test")
    }
    "not change an absolute URL with no user-info" in {
      val uri = Url.parse("http://example.com/test")
      uri.mapPassword(_ + "2") should equal(uri)
    }
  }

  "removeUserInfo" should {
    "not change a relative URL" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye")
      uri.removeUserInfo() should equal(uri)
    }
    "not change a URL without authority" in {
      val uri = Url.parse("mailto:me@example.com")
      uri.removeUserInfo() should equal(uri)
    }
    "remove a user and password in an absolute URL" in {
      val uri = Url.parse("http://me:password@example.com/test").removeUserInfo()
      uri.user should equal(None)
      uri.password should equal(None)
      uri.toString should equal("http://example.com/test")
    }
    "remove a user from an absolute URL" in {
      val uri = Url.parse("http://me@example.com/test").removeUserInfo()
      uri.user should equal(None)
      uri.password should equal(None)
      uri.toString should equal("http://example.com/test")
    }
    "not change an absolute URL with no user-info" in {
      val uri = Url.parse("http://example.com/test")
      uri.removeUserInfo() should equal(uri)
    }
  }

  "removePassword" should {
    "not change a relative URL" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye")
      uri.removePassword() should equal(uri)
    }
    "not change a URL without authority" in {
      val uri = Url.parse("mailto:me@example.com")
      uri.removePassword() should equal(uri)
    }
    "remove the password in an absolute URL" in {
      val uri = Url.parse("http://me:password@example.com/test").removePassword()
      uri.password should equal(None)
      uri.toString should equal("http://me@example.com/test")
    }
    "not change an absolute URL without a password" in {
      val uri = Url.parse("http://me@example.com/test")
      uri.removePassword() should equal(uri)
    }
    "not change an absolute URL with no user-info" in {
      val uri = Url.parse("http://example.com/test")
      uri.removePassword() should equal(uri)
    }
  }
}
