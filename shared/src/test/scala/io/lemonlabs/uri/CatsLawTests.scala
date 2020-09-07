package io.lemonlabs.uri

import cats.kernel.laws.discipline.OrderTests
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.Checkers
import org.typelevel.discipline.Laws
import cats.implicits._
import cats.Eq
import cats.laws.discipline.eq._
import cats.laws.discipline.{ContravariantTests, ExhaustiveCheck}
import io.lemonlabs.uri.typesafe.{Fragment, PathPart, QueryKey, QueryValue, TraversableParams}

class CatsLawTests extends AnyFunSuite with Checkers with UriScalaCheckGenerators {
  // When upgrading to scalatest 3.1.0, this should come from discipline-scalatest
  def checkAll(name: String, ruleSet: Laws#RuleSet): Unit = {
    for ((id, prop) <- ruleSet.all.properties)
      registerTest(s"$name.$id") {
        check(prop)
      }
  }

  implicit def eqFragment[A: ExhaustiveCheck]: Eq[Fragment[A]] =
    Eq.by[Fragment[A], A => Option[String]](_.fragment)

  implicit def eqPathPart[A: ExhaustiveCheck]: Eq[PathPart[A]] =
    Eq.by[PathPart[A], A => String](_.path)

  implicit def eqQueryValue[A: ExhaustiveCheck]: Eq[QueryValue[A]] =
    Eq.by[QueryValue[A], A => Option[String]](_.queryValue)

  implicit def eqQueryKey[A: ExhaustiveCheck]: Eq[QueryKey[A]] =
    Eq.by[QueryKey[A], A => String](_.queryKey)

  implicit def eqTraversableParams[A: ExhaustiveCheck]: Eq[TraversableParams[A]] =
    Eq.by[TraversableParams[A], A => Vector[(String, Option[String])]](_.toVector)

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
  checkAll("ScpLikeUrl.OrderTests", OrderTests[ScpLikeUrl].order)
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
  checkAll("Fragment.Contravariant", ContravariantTests[Fragment].contravariant[Boolean, Boolean, Boolean])
  checkAll("PathPart.Contravariant", ContravariantTests[PathPart].contravariant[Boolean, Boolean, Boolean])
  checkAll("QueryValue.Contravariant", ContravariantTests[QueryValue].contravariant[Boolean, Boolean, Boolean])
  checkAll("QueryKey.Contravariant", ContravariantTests[QueryKey].contravariant[Boolean, Boolean, Boolean])
  checkAll(
    "TraversableParams.Contravariant",
    ContravariantTests[TraversableParams].contravariant[Boolean, Boolean, Boolean]
  )
}
