package io.lemonlabs.uri

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TransformTests extends AnyWordSpec with Matchers {
  "mapQuery" should {
    "transform query params" in {
      val uri = Url.parse("/test?param_1=hello&param_2=goodbye&param_3=false")
      val uri2 = uri.mapQuery {
        case (k, v) => (k, v map (_ + "TEST"))
      }
      uri2.toString should equal("/test?param_1=helloTEST&param_2=goodbyeTEST&param_3=falseTEST")
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
      val uri2 = uri.filterQuery(p => false)
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
  }
}
