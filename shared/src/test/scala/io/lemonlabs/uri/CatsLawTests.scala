package io.lemonlabs.uri

import cats.kernel.laws.discipline.OrderTests
import org.scalatest.FunSuite
import org.scalatestplus.scalacheck.Checkers
import org.typelevel.discipline.Laws

import cats.implicits._

class CatsLawTests extends FunSuite with Checkers with UriScalaCheckGenerators {
  // When upgrading to scalatest 3.1.0, this should come from discipline-scalatest
  def checkAll(name: String, ruleSet: Laws#RuleSet): Unit = {
    for ((id, prop) <- ruleSet.all.properties)
      registerTest(s"$name.$id") {
        check(prop)
      }
  }

  // Note: OrderTests also run EqTests, so no need to also kick off EqTests here
  checkAll("Uri.OrderTests", OrderTests[Uri].order)
  checkAll("Url.OrderTests", OrderTests[Url].order)
  checkAll("RelativeUrl.OrderTests", OrderTests[RelativeUrl].order)
  checkAll("UrlWithAuthority.OrderTests", OrderTests[UrlWithAuthority].order)
  checkAll("ProtocolRelativeUrl.OrderTests", OrderTests[ProtocolRelativeUrl].order)
  checkAll("AbsoluteUrl.OrderTests", OrderTests[AbsoluteUrl].order)
  checkAll("UrlWithoutAuthority.OrderTests", OrderTests[UrlWithoutAuthority].order)
  checkAll("SimpleUrlWithoutAuthority.OrderTests", OrderTests[SimpleUrlWithoutAuthority].order)
  checkAll("DataUrl.OrderTests", OrderTests[DataUrl].order)
  checkAll("Urn.OrderTests", OrderTests[Urn].order)
  checkAll("Authority.OrderTests", OrderTests[Authority].order)
  checkAll("UserInfo.OrderTests", OrderTests[UserInfo].order)
  checkAll("Host.OrderTests", OrderTests[Host].order)
  checkAll("DomainName.OrderTests", OrderTests[DomainName].order)
  checkAll("IpV4.OrderTests", OrderTests[IpV4].order)
  checkAll("IpV6.OrderTests", OrderTests[IpV6].order)
  checkAll("MediaType.OrderTests", OrderTests[MediaType].order)
  checkAll("Path.OrderTests", OrderTests[Path].order)
  checkAll("UrlPath.OrderTests", OrderTests[UrlPath].order)
  checkAll("AbsoluteOrEmptyPath.OrderTests", OrderTests[AbsoluteOrEmptyPath].order)
  checkAll("RootlessPath.OrderTests", OrderTests[RootlessPath].order)
  checkAll("AbsolutePath.OrderTests", OrderTests[AbsolutePath].order)
  checkAll("UrnPath.OrderTests", OrderTests[UrnPath].order)
  checkAll("QueryString.OrderTests", OrderTests[QueryString].order)
}
