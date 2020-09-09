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
}
