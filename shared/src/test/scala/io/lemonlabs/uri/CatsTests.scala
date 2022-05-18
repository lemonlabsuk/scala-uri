package io.lemonlabs.uri

import org.scalatest.flatspec.AnyFlatSpec
import cats.implicits._
import org.scalatest.matchers.should.Matchers
import cats.kernel.{Comparison, Eq}

class CatsTests extends AnyFlatSpec with Matchers {
  "Eq" should "be supported for Uri" in {
    val uri = Uri.parse("https://typelevel.org/cats/")
    val uri2: Uri = AbsoluteUrl.parse("https://typelevel.org/cats/")

    uri eqv uri2 should equal(true)
  }

  it should "be supported for unordered query params Uri" in {
    import Uri.unordered._

    val uri = Uri.parse("https://typelevel.org/cats/?a=1&b=two")
    val uri2: Uri = AbsoluteUrl.parse("https://typelevel.org/cats/?b=two&a=1")

    uri eqv uri2 should equal(true)
  }

  it should "be supported for Url" in {
    val uri = Url.parse("https://typelevel.org/cats/")
    val uri2: Url = AbsoluteUrl.parse("https://typelevel.org/cats/")

    uri eqv uri2 should equal(true)
  }

  it should "be supported unordered query params Url" in {
    import Url.unordered._

    val uri = Url.parse("https://typelevel.org/cats/?a=1&b=two")
    val uri2: Url = AbsoluteUrl.parse("https://typelevel.org/cats/?b=two&a=1")

    uri eqv uri2 should equal(true)
  }

  it should "be supported for RelativeUrl" in {
    val uri: Url = RelativeUrl.parse("/cats2/")
    val uri2: Url = RelativeUrl.parse("/cats/")

    uri eqv uri2 should equal(false)
  }

  it should "be supported for unordered query params RelativeUrl" in {
    import RelativeUrl.unordered._

    val uri = RelativeUrl.parse("/cats2/?b=two&a=1")
    val uri2 = RelativeUrl.parse("/cats/?a=1&b=two")

    uri eqv uri2 should equal(false)
  }

  it should "be supported for UrlWithAuthority" in {
    val uri = UrlWithAuthority.parse("https://typelevel.org/cats/")
    val uri2: UrlWithAuthority = AbsoluteUrl.parse("https://typelevel.org/cats/")

    uri eqv uri2 should equal(true)
  }

  it should "be supported for unordered query params UrlWithAuthority" in {
    import UrlWithAuthority.unordered._

    val uri = UrlWithAuthority.parse("https://typelevel.org/cats/?a=1&b=two")
    val uri2: UrlWithAuthority = AbsoluteUrl.parse("https://typelevel.org/cats/?b=two&a=1")

    uri eqv uri2 should equal(true)
  }

  it should "be supported for ProtocolRelativeUrl" in {
    val uri = ProtocolRelativeUrl.parse("//typelevel.org/cats/")
    val uri2 = ProtocolRelativeUrl.parse("//typelevel.org/cats/?different=true")

    uri =!= uri2 should equal(true)
  }

  it should "be supported for unordered query params ProtocolRelativeUrl" in {
    import ProtocolRelativeUrl.unordered._

    val uri = ProtocolRelativeUrl.parse("//typelevel.org/cats/?b=two&a=1")
    val uri2 = ProtocolRelativeUrl.parse("//typelevel.org/cats/?a=1&b=two")

    uri =!= uri2 should equal(false)
  }

  it should "be supported for AbsoluteUrl" in {
    val uri = AbsoluteUrl.parse("https://typelevel.org/cats/")
    val uri2 = AbsoluteUrl.parse("https://typelevel.org/cats/")

    uri eqv uri2 should equal(true)
  }

  it should "be supported for unordered query params AbsoluteUrl" in {
    import AbsoluteUrl.unordered._

    val uri = AbsoluteUrl.parse("https://typelevel.org/cats/?a=1&b=two")
    val uri2 = AbsoluteUrl.parse("https://typelevel.org/cats/?b=two&a=1")

    uri eqv uri2 should equal(true)
  }

  it should "be supported for UrlWithoutAuthority" in {
    val uri = UrlWithoutAuthority.parse("mailto:someone@somewhere.com")
    val uri2 = UrlWithoutAuthority.parse("mailto:someoneelse@somewhereelse.com")

    uri eqv uri2 should equal(false)
  }

  it should "be supported for unordered query params UrlWithoutAuthority" in {
    import UrlWithoutAuthority.unordered._

    val uri = UrlWithoutAuthority.parse("mailto:someone@somewhere.com?b=two&a=1")
    val uri2 = UrlWithoutAuthority.parse("mailto:someoneelse@somewhereelse.com?a=1&b=two")

    uri eqv uri2 should equal(false)
  }

  it should "be supported for SimpleUrlWithoutAuthority" in {
    val uri = SimpleUrlWithoutAuthority.parse("mailto:someone@somewhere.com")
    val uri2 = SimpleUrlWithoutAuthority.parse("mailto:someone@somewhere.com")

    uri eqv uri2 should equal(true)
  }

  it should "be supported for unordered query params SimpleUrlWithoutAuthority" in {
    import SimpleUrlWithoutAuthority.unordered._

    val uri = SimpleUrlWithoutAuthority.parse("mailto:someone@somewhere.com?a=1&b=two")
    val uri2 = SimpleUrlWithoutAuthority.parse("mailto:someone@somewhere.com?b=two&a=1")

    uri eqv uri2 should equal(true)
  }

  it should "be supported for DataUrl" in {
    val uri = DataUrl.parse("data:,A%20brief%20note")
    val uri2 = DataUrl.parse("data:,Another%20brief%20note")

    uri =!= uri2 should equal(true)
  }

  it should "be supported for ScpLikeUrl" in {
    val uri = ScpLikeUrl.parse("root@host:/root/file.tar.gz")
    val uri2 = ScpLikeUrl.parse("root@host:/root/file2.tar.gz")

    uri =!= uri2 should equal(true)
  }

  it should "be supported for Urn" in {
    val uri = Urn.parse("urn:cats:1")
    val uri2 = Urn.parse("urn:cats:2")

    uri =!= uri2 should equal(true)
  }

  it should "be supported for Authority" in {
    val authority = Authority.parse("typelevel.org:443")
    val authority2 = Authority.parse("typelevel.org:443")

    authority eqv authority2 should equal(true)
  }

  it should "be supported for UserInfo" in {
    val userInfo = UserInfo("user", "password")
    val userInfo2 = UserInfo("user", "password2")

    userInfo =!= userInfo2 should equal(true)
  }

  it should "be supported for Host" in {
    val host = Host.parse("typelevel.org")
    val host2 = Host.parse("typelevel.org")

    host =!= host2 should equal(false)
  }

  it should "be supported for DomainName" in {
    val host = DomainName.parse("typelevel.org")
    val host2 = DomainName.parse("www.typelevel.org")

    host =!= host2 should equal(true)
  }

  it should "be supported for Ipv4" in {
    val host = IpV4.parse("8.8.8.8")
    val host2 = IpV4.parse("8.8.8.8")

    host eqv host2 should equal(true)
  }

  it should "be supported for Ipv6" in {
    val host = IpV6.parse("[1f4:0:0:1e::1]")
    val host2 = IpV6.parse("[1f4:0:0:1e::1]")

    host eqv host2 should equal(true)
  }

  it should "be supported for MediaType" in {
    val mediaType = MediaType("text/plain".some, Vector("charset" -> "utf8"))
    val mediaType2 = MediaType("text/plain".some, Vector.empty)

    mediaType eqv mediaType2 should equal(false)
  }

  it should "be supported for Path" in {
    val path = Path.parse("/cats/")
    val path2 = Path.parse("/cats/")

    path eqv path2 should equal(true)
  }

  it should "be supported for UrlPath" in {
    val path = UrlPath.parse("/cats/")
    val path2 = UrlPath.parse("/cats/")

    path eqv path2 should equal(true)
  }

  it should "be supported for AbsoluteOrEmptyPath" in {
    val path: AbsoluteOrEmptyPath = AbsolutePath.fromParts("cats")
    val path2: AbsoluteOrEmptyPath = AbsolutePath.fromParts("cats")

    path eqv path2 should equal(true)
  }

  it should "be supported for RootlessPath" in {
    val path = RootlessPath.fromParts("cats")
    val path2 = RootlessPath.fromParts("cats2")

    path =!= path2 should equal(true)
  }

  it should "be supported for AbsolutePath" in {
    val path = AbsolutePath.fromParts("cats")
    val path2 = AbsolutePath.fromParts("cats")

    path eqv path2 should equal(true)
  }

  it should "be supported for UrnPath" in {
    val path = UrnPath.parse("cats:1")
    val path2 = UrnPath.parse("cats:2")

    path =!= path2 should equal(true)
  }

  it should "be supported for QueryString" in {
    val qs = QueryString.parse("a=1&b=2")
    val qs2 = QueryString.parse("a=1&b=2")

    qs eqv qs2 should equal(true)
  }

  it should "be supported for unordered QueryString" in {
    val qs = QueryString.parse("a=1&b=2")
    val qs2 = QueryString.parse("b=2&a=1")

    import QueryString.unordered._
    qs eqv qs2 should equal(true)
  }

  it should "be supported for ordered QueryString" in {
    val qs = QueryString.parse("a=1&b=2")
    val qs2 = QueryString.parse("b=2&a=1")

    qs eqv qs2 should equal(false)
  }

  "Show" should "be supported for Uri" in {
    val uri = Uri.parse("https://typelevel.org/cats/")
    uri.show should equal("https://typelevel.org/cats/")
  }

  it should "be supported for Url" in {
    val uri = Url.parse("https://typelevel.org/cats/")
    uri.show should equal("https://typelevel.org/cats/")
  }

  it should "be supported for RelativeUrl" in {
    val uri: Url = RelativeUrl.parse("/cats2/")
    uri.show should equal("/cats2/")
  }

  it should "be supported for UrlWithAuthority" in {
    val uri = UrlWithAuthority.parse("https://typelevel.org/cats/")
    uri.show should equal("https://typelevel.org/cats/")
  }

  it should "be supported for ProtocolRelativeUrl" in {
    val uri = ProtocolRelativeUrl.parse("//typelevel.org/cats/")
    uri.show should equal("//typelevel.org/cats/")
  }

  it should "be supported for AbsoluteUrl" in {
    val uri = AbsoluteUrl.parse("https://typelevel.org/cats/")
    uri.show should equal("https://typelevel.org/cats/")
  }

  it should "be supported for UrlWithoutAuthority" in {
    val uri = UrlWithoutAuthority.parse("mailto:someone@somewhere.com")
    uri.show should equal("mailto:someone@somewhere.com")
  }

  it should "be supported for SimpleUrlWithoutAuthority" in {
    val uri = SimpleUrlWithoutAuthority.parse("mailto:someone@somewhere.com")
    uri.show should equal("mailto:someone@somewhere.com")
  }

  it should "be supported for DataUrl" in {
    val uri = DataUrl.parse("data:,A%20brief%20note")
    uri.show should equal("data:,A%20brief%20note")
  }

  it should "be supported for Urn" in {
    val uri = Urn.parse("urn:cats:1")
    uri.show should equal("urn:cats:1")
  }

  it should "be supported for Authority" in {
    val authority = Authority.parse("typelevel.org:443")
    authority.show should equal("typelevel.org:443")
  }

  it should "be supported for UserInfo" in {
    val userInfo = UserInfo("user", "password")
    userInfo.show should equal("user:password")
  }

  it should "be supported for Host" in {
    val host = Host.parse("typelevel.org")
    host.show should equal("typelevel.org")
  }

  it should "be supported for DomainName" in {
    val host = DomainName.parse("typelevel.org")
    host.show should equal("typelevel.org")
  }

  it should "be supported for Ipv4" in {
    val host = IpV4.parse("8.8.8.8")
    host.show should equal("8.8.8.8")
  }

  it should "be supported for Ipv6" in {
    val host = IpV6.parse("[1f4:0:0:1e::1]")
    host.show should equal("[1f4:0:0:1e::1]")
  }

  it should "be supported for MediaType" in {
    val mediaType = MediaType("text/plain".some, Vector("charset" -> "utf8"))
    mediaType.show should equal("text/plain;charset=utf8")
  }

  it should "be supported for Path" in {
    val path = Path.parse("/cats/")
    path.show should equal("/cats/")
  }

  it should "be supported for UrlPath" in {
    val path = UrlPath.parse("/cats/")
    path.show should equal("/cats/")
  }

  it should "be supported for AbsoluteOrEmptyPath" in {
    val path: AbsoluteOrEmptyPath = AbsolutePath.fromParts("cats")
    path.show should equal("/cats")
  }

  it should "be supported for RootlessPath" in {
    val path = RootlessPath.fromParts("cats")
    path.show should equal("cats")
  }

  it should "be supported for AbsolutePath" in {
    val path = AbsolutePath.fromParts("cats")
    path.show should equal("/cats")
  }

  it should "be supported for UrnPath" in {
    val path = UrnPath.parse("cats:1")
    path.show should equal("cats:1")
  }

  it should "be supported for QueryString" in {
    val qs = QueryString.parse("a=1&b=2")
    qs.show should equal("a=1&b=2")
  }

  "Order" should "be supported for Uri" in {
    val uri = Uri.parse("https://typelevel.org/cats2/")
    val uri2: Uri = AbsoluteUrl.parse("https://typelevel.org/cats/")
    uri comparison uri2 should equal(Comparison.GreaterThan)
  }

  it should "be supported for Url" in {
    val uri = Url.parse("https://typelevel.org/cats/")
    val uri2: Url = AbsoluteUrl.parse("https://typelevel.org/cats/")
    uri comparison uri2 should equal(Comparison.EqualTo)
  }

  it should "be supported for RelativeUrl" in {
    val uri: Url = RelativeUrl.parse("/cats2/")
    val uri2: Url = RelativeUrl.parse("/cats/")
    uri comparison uri2 should equal(Comparison.GreaterThan)
  }

  it should "be supported for UrlWithAuthority" in {
    val uri = UrlWithAuthority.parse("https://typelevel.org/cats/")
    val uri2: UrlWithAuthority = AbsoluteUrl.parse("https://typelevel.org/cats/")
    uri comparison uri2 should equal(Comparison.EqualTo)
  }

  it should "be supported for ProtocolRelativeUrl" in {
    val uri = ProtocolRelativeUrl.parse("//typelevel.org/cats/")
    val uri2 = ProtocolRelativeUrl.parse("//typelevel.org/cats/?different=true")
    uri comparison uri2 should equal(Comparison.LessThan)
  }

  it should "be supported for AbsoluteUrl" in {
    val uri = AbsoluteUrl.parse("https://typelevel.org/cats/")
    val uri2 = AbsoluteUrl.parse("https://typelevel.org/cats/")
    uri comparison uri2 should equal(Comparison.EqualTo)
  }

  it should "be supported for UrlWithoutAuthority" in {
    val uri = UrlWithoutAuthority.parse("mailto:someone@somewhere.com")
    val uri2 = UrlWithoutAuthority.parse("mailto:someoneelse@somewhereelse.com")
    uri comparison uri2 should equal(Comparison.LessThan)
  }

  it should "be supported for SimpleUrlWithoutAuthority" in {
    val uri = SimpleUrlWithoutAuthority.parse("mailto:someone@somewhere.com")
    val uri2 = SimpleUrlWithoutAuthority.parse("mailto:someone@somewhere.com")
    uri comparison uri2 should equal(Comparison.EqualTo)
  }

  it should "be supported for DataUrl" in {
    val uri = DataUrl.parse("data:text;,A%20brief%20note")
    val uri2 = DataUrl.parse("data:text;base64,R0lGODdh")
    uri comparison uri2 should equal(Comparison.LessThan)
  }

  "Order" should "be supported for Urn" in {
    val uri = Urn.parse("urn:cats:1")
    val uri2 = Urn.parse("urn:cats:2")
    uri comparison uri2 should equal(Comparison.LessThan)
  }

  it should "be supported for Authority" in {
    val authority = Authority.parse("typelevel.org:443")
    val authority2 = Authority.parse("typelevel.org:443")
    authority comparison authority2 should equal(Comparison.EqualTo)
  }

  it should "be supported for UserInfo" in {
    val userInfo = UserInfo("user", "password")
    val userInfo2 = UserInfo("user", "password2")
    userInfo comparison userInfo2 should equal(Comparison.LessThan)
  }

  it should "be supported for Host" in {
    val host = Host.parse("typelevel.org")
    val host2 = Host.parse("typelevel.org")
    host comparison host2 should equal(Comparison.EqualTo)
  }

  it should "be supported for DomainName" in {
    val host = DomainName.parse("typelevel.org")
    val host2 = DomainName.parse("www.typelevel.org")
    host comparison host2 should equal(Comparison.LessThan)
  }

  it should "be supported for Ipv4" in {
    val host = IpV4.parse("8.8.8.8")
    val host2 = IpV4.parse("8.8.8.8")
    host comparison host2 should equal(Comparison.EqualTo)
  }

  it should "be supported for Ipv6" in {
    val host = IpV6.parse("[1f4:0:0:1e::1]")
    val host2 = IpV6.parse("[1f4:0:0:1e::1]")
    host comparison host2 should equal(Comparison.EqualTo)
  }

  it should "be supported for MediaType" in {
    val mediaType = MediaType("text/plain".some, Vector("charset" -> "utf8"))
    val mediaType2 = MediaType("text/plain".some, Vector.empty)
    mediaType comparison mediaType2 should equal(Comparison.GreaterThan)
  }

  it should "be supported for Path" in {
    val path = Path.parse("/cats/")
    val path2 = Path.parse("/cats/")
    path comparison path2 should equal(Comparison.EqualTo)
  }

  it should "be supported for UrlPath" in {
    val path = UrlPath.parse("/cats/")
    val path2 = UrlPath.parse("/cats/")
    path comparison path2 should equal(Comparison.EqualTo)
  }

  it should "be supported for AbsoluteOrEmptyPath" in {
    val path: AbsoluteOrEmptyPath = AbsolutePath.fromParts("cats")
    val path2: AbsoluteOrEmptyPath = AbsolutePath.fromParts("cats")
    path comparison path2 should equal(Comparison.EqualTo)
  }

  it should "be supported for RootlessPath" in {
    val path = RootlessPath.fromParts("cats")
    val path2 = RootlessPath.fromParts("cats2")
    path comparison path2 should equal(Comparison.LessThan)
  }

  it should "be supported for AbsolutePath" in {
    val path = AbsolutePath.fromParts("cats")
    val path2 = AbsolutePath.fromParts("cats")
    path comparison path2 should equal(Comparison.EqualTo)
  }

  it should "be supported for UrnPath" in {
    val path = UrnPath.parse("cats:1")
    val path2 = UrnPath.parse("cats:2")
    path comparison path2 should equal(Comparison.LessThan)
  }

  it should "be supported for QueryString" in {
    val qs = QueryString.parse("a=1&b=2")
    val qs2 = QueryString.parse("a=1&b=2")
    qs comparison qs2 should equal(Comparison.EqualTo)
  }
}
