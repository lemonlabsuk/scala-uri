package io.lemonlabs.uri

import java.util.Base64

import cats.Show
import io.lemonlabs.uri.encoding.PercentEncoder
import org.scalacheck.{Arbitrary, Cogen, Gen}
import cats.implicits._
import io.lemonlabs.uri.typesafe.{Fragment, PathPart, QueryKey, QueryValue, TraversableParams}
import org.scalacheck.Gen.{const, some}

trait UriScalaCheckGenerators {
  private val unicodeChar: Gen[Char] = Gen.frequency(
    (9, Gen.asciiChar),
    (1, Arbitrary.arbChar.arbitrary)
  )

  private val genDelimChar: Gen[Char] =
    Gen.oneOf(":/?#[]@".toSeq)

  private val subDelimChar: Gen[Char] =
    Gen.oneOf("!$&'()*+,;=".toSeq)

  private val reservedChar: Gen[Char] =
    Gen.oneOf(genDelimChar, subDelimChar)

  private val percentEncodedChar: Gen[String] = {
    val pe = PercentEncoder()
    unicodeChar.map(pe.encodeChar)
  }

  private val unreservedChar: Gen[Char] = Gen.frequency(
    (9, Gen.alphaNumChar),
    (1, Gen.oneOf("-._~".toSeq))
  )

  private val pChar: Gen[String] = Gen.frequency(
    (50, unreservedChar.map(_.toString)),
    (1, percentEncodedChar),
    (1, subDelimChar.map(_.toString)),
    (1, Gen.oneOf(":", "@"))
  )

  private def strGen[T](charGen: Gen[T], min: Int = 0, max: Int = 10): Gen[String] =
    Gen.chooseNum(min, max).flatMap { length =>
      Gen.listOfN(length, charGen).map(_.mkString)
    }

  private val userOrPass: Gen[String] = strGen(
    Gen.frequency(
      (50, unreservedChar),
      (1, percentEncodedChar),
      (1, subDelimChar)
    ),
    max = 15
  )
  private val maybeUserOrPass: Gen[Option[String]] =
    Gen.oneOf(const(None), some(userOrPass))

  // format: off
  private val randScheme: Gen[String] = Gen.oneOf(
    "aaa", "aaas", "about", "acap", "acct", "acr", "adiumxtra", "afp", "afs", "aim", "apt", "attachment", "aw",
    "amss", "barion", "beshare", "bitcoin", "blob", "bolo", "callto", "cap", "chrome", "chrome-extension",
    "com-eventbrite-attendee", "cid", "coap", "coaps", "content", "crid", "cvs", "dab", "data", "dav", "dict",
    "dlna-playsingle", "dlna-playcontainer", "dns", "dntp", "drm", "dtn", "dvb", "ed2k", "example", "facetime",
    "fax", "feed", "file", "filesystem", "finger", "fish", "fm", "ftp", "geo", "gg", "git", "gizmoproject", "go",
    "gopher", "gtalk", "h323", "hcp", "http", "https", "iax", "icap", "icon", "im", "imap", "info", "iotdisco",
    "ipn", "ipp", "ipps", "irc", "irc6", "irc", "ircs", "irc", "iris", "iris.beep", "iris.xpc", "iris.xpcs",
    "iris.lws", "itms", "jabber", "jar", "jms", "keyparc", "lastfm", "ldap", "ldaps", "ldap", "magnet",
    "mailserver", "mailto", "maps", "market", "message", "mid", "mms", "modem", "ms-help", "ms-settings",
    "ms-settings-airplanemode", "ms-settings-bluetooth", "ms-settings-camera", "ms-settings-cellular",
    "ms-settings-cloudstorage", "ms-settings-emailandaccounts", "ms-settings-language", "ms-settings-location",
    "ms-settings-lock", "ms-settings-nfctransactions", "ms-settings-notifications", "ms-settings-power",
    "ms-settings-privacy", "ms-settings-proximity", "ms-settings-screenrotation", "ms-settings-wifi",
    "ms-settings-workplace", "msnim", "msrp", "msrps", "mtqp", "mumble", "mupdate", "mvn", "news", "resource",
    "nfs", "ni", "nih", "nntp", "notes", "oid", "opaquelocktoken", "pack", "palm", "paparazzi", "pkcs11",
    "platform", "pop", "pres", "prospero", "proxy", "psyc", "query", "redis", "rediss", "reload", "res",
    "resource", "rmi", "rsync", "rtmfp", "rtmp", "rtsp", "secondlife", "service", "session", "sftp", "sgn",
    "shttp", "sieve", "sip", "sips", "sip", "skype", "smb", "sms", "citation needed", "snews", "snmp",
    "soap.beep", "soap.beeps", "soldat", "spotify", "ssh", "steam", "stun", "stuns", "svn", "tag", "teamspeak",
    "tel", "telnet", "tftp", "things", "thismessage", "tn3270", "tip", "turn", "turns", "tv", "udp", "unreal",
    "urn", "ut2004", "vemmi", "ventrilo", "videotex", "view-source", "wais", "webcal", "ws", "wss", "wtai",
    "wyciwyg", "xcon", "xcon-userid", "xfire", "xmlrpc.beep", "xmlrpc.beeps", "xmpp", "xri", "ymsgr", "z39.50",
    "z39.50r", "z39.50s", "app", "doi", "javascript", "jdbc", "odbc", "stratum", "vnc", "web+abc"
  )
  // format: off

  implicit val randIpV6:Arbitrary[IpV6] =  {
    val piece = Gen.chooseNum[Int](0, Char.MaxValue)
    Arbitrary(Gen.zip(piece, piece, piece, piece, piece, piece, piece, piece).map {
      case (a, b, c, d, e, f, g, h) => IpV6.apply(a, b, c, d, e, f, g, h)
    })
  }

  implicit val randIpV4: Arbitrary[IpV4] =  {
    val octet = Gen.chooseNum(0, 255)
    Arbitrary(Gen.zip(octet, octet, octet, octet).map {
      case (a, b, c, d) => IpV4(a, b, c, d)
    })
  }

  implicit val randDomainName: Arbitrary[DomainName] =  {
    val chars: Gen[String] = Gen.frequency(
      (50, unreservedChar.map(_.toString)),
      (1, percentEncodedChar),
      (1, subDelimChar.map(_.toString))
    )
    Arbitrary(strGen(chars, max = 15).map(DomainName.parse))
  }

  implicit val randHost: Arbitrary[Host] =
    Arbitrary(Gen.oneOf(randDomainName.arbitrary, randIpV4.arbitrary, randIpV6.arbitrary))
  
  implicit val randUserInfo:  Arbitrary[UserInfo] =  {
    
    val gen = for {
      user <- userOrPass
      pass <- maybeUserOrPass
    } yield UserInfo(user, pass)
    Arbitrary(gen)
  }

  implicit val randAuthority: Arbitrary[Authority] = {
    val gen = for {
      userInfo <- Gen.option(randUserInfo.arbitrary)
      host <- randHost.arbitrary
      port <- Gen.frequency(
        (1, Gen.some(Gen.chooseNum(0, 65535))),
        (9, Gen.const(None))
      )
    } yield Authority(userInfo, host, port)
    Arbitrary(gen)
  }

  implicit val randMediaType: Arbitrary[MediaType] = {
    val restrictedName = strGen(
      Gen.frequency(
        (9, Gen.alphaNumChar),
        (1, Gen.oneOf("!#$&-^_.+"))
      ))
    val parameter = Gen.oneOf(restrictedName.map(_ -> ""), Gen.zip(restrictedName, restrictedName))
    val gen = for {
      value <- Gen.oneOf(
        Gen.const(None),
        Gen.some(restrictedName), // Just type
        Gen.listOfN(2, restrictedName).map(parts => Some(parts.mkString("/"))) // Type and subtype
      )
      params <- Gen.chooseNum(0, 3).flatMap(size => Gen.listOfN(size, parameter).map(_.toVector))
    } yield MediaType(value, params)
    Arbitrary(gen)
  }

  private val randSegments: Gen[List[String]] = for {
    size <- Gen.chooseNum(0, 5)
    elems <- Gen.listOfN(size, strGen(pChar))
  } yield elems

  implicit val randUrnPath: Arbitrary[UrnPath] = {
    val gen = for {
      nss <- strGen(pChar)
      nid <- strGen(pChar)
    } yield UrnPath(nid, nss)
    Arbitrary(gen)
  }

  implicit val randAbsolutePath: Arbitrary[AbsolutePath] =
    Arbitrary(randSegments.map(parts => AbsolutePath(parts.toVector)))

  implicit val randRootlessPath: Arbitrary[RootlessPath] =
    Arbitrary(randSegments.map(parts => RootlessPath(parts.toVector)))

  implicit val randAbsoluteOrEmptyPath: Arbitrary[AbsoluteOrEmptyPath] =
    Arbitrary(Gen.oneOf(randAbsolutePath.arbitrary, Gen.const(EmptyPath)))

  implicit val randUrlPath:  Arbitrary[UrlPath] =
    Arbitrary(Gen.oneOf(randAbsoluteOrEmptyPath.arbitrary, randRootlessPath.arbitrary))

  implicit val randPath:  Arbitrary[Path] =
    Arbitrary(Gen.oneOf(randUrlPath.arbitrary, randUrnPath.arbitrary))

  private val randQueryStringParam: Gen[(String, Option[String])] =  {
    val chars = Gen.frequency(
      (20, pChar),
      (1, Gen.oneOf("/", "?"))
    )
    for {
      key <- strGen(chars)
      value <- Gen.some(strGen(chars, max = 30))
    } yield (key, value)
  }

  implicit val randQueryString: Arbitrary[QueryString] = {
    val gen = for {
      numParams <- Gen.chooseNum(0, 5)
      params <- Gen.listOfN(numParams, randQueryStringParam)
    } yield QueryString(params.toVector)
    Arbitrary(gen)
  }

  private val randFragment: Gen[Option[String]] = {
    val chars = Gen.frequency(
      (20, pChar),
      (1, Gen.oneOf("/", "?"))
    )
    Gen.frequency(
      (8, Gen.const(None)),
      (2, Gen.some(strGen(chars)))
    )
  }

  implicit val randDataUrl: Arbitrary[DataUrl] =  {
    val base64Bytes = strGen(unicodeChar, min = 10, max = 30).map(s => Base64.getEncoder.withoutPadding().encode(s.getBytes()))
    val plainBytes = strGen(unicodeChar, min = 10, max = 30).map(_.getBytes)
    Arbitrary(Gen.oneOf(
      Gen.zip(randMediaType.arbitrary, Gen.const(true), base64Bytes).map((DataUrl.apply _).tupled),
      Gen.zip(randMediaType.arbitrary, Gen.const(false), plainBytes).map((DataUrl.apply _).tupled)
    ))
  }

  implicit val randScpLikeUrl: Arbitrary[ScpLikeUrl] =
    Arbitrary(Gen.zip(maybeUserOrPass, randHost.arbitrary, randUrlPath.arbitrary).map((ScpLikeUrl.apply _).tupled))
  
  implicit val randSimpleUrlWithoutAuthority: Arbitrary[SimpleUrlWithoutAuthority] =
    Arbitrary(Gen.zip(randScheme, randUrlPath.arbitrary, randQueryString.arbitrary, randFragment).map((SimpleUrlWithoutAuthority.apply _).tupled))

  implicit val randUrlWithoutAuthority: Arbitrary[UrlWithoutAuthority] =
    Arbitrary(Gen.oneOf(randSimpleUrlWithoutAuthority.arbitrary, randDataUrl.arbitrary))

  implicit val randAbsoluteUrl: Arbitrary[AbsoluteUrl] =
    Arbitrary(Gen.zip(randScheme, randAuthority.arbitrary, randAbsoluteOrEmptyPath.arbitrary, randQueryString.arbitrary, randFragment).map((AbsoluteUrl.apply _).tupled))

  implicit val randProtocolRelativeUrl: Arbitrary[ProtocolRelativeUrl] =
    Arbitrary(Gen.zip(randAuthority.arbitrary, randAbsoluteOrEmptyPath.arbitrary, randQueryString.arbitrary, randFragment).map((ProtocolRelativeUrl.apply _).tupled))

  implicit val randUrlWithAuthority: Arbitrary[UrlWithAuthority] =
    Arbitrary(Gen.oneOf(randAbsoluteUrl.arbitrary, randProtocolRelativeUrl.arbitrary))

  implicit val randRelativeUrl: Arbitrary[RelativeUrl] =
    Arbitrary(Gen.zip(randUrlPath.arbitrary, randQueryString.arbitrary, randFragment).map((RelativeUrl.apply _).tupled))

  implicit val randUrl: Arbitrary[Url] =
    Arbitrary(Gen.oneOf(randDataUrl.arbitrary, randSimpleUrlWithoutAuthority.arbitrary, randAbsoluteUrl.arbitrary, randProtocolRelativeUrl.arbitrary, randRelativeUrl.arbitrary))

  implicit val randUrn: Arbitrary[Urn] =
    Arbitrary(randUrnPath.arbitrary.map(Urn.apply))

  implicit val randUri: Arbitrary[Uri] = Arbitrary(
    Gen.oneOf(randUrn.arbitrary, randDataUrl.arbitrary, randSimpleUrlWithoutAuthority.arbitrary, randAbsoluteUrl.arbitrary, randProtocolRelativeUrl.arbitrary, randRelativeUrl.arbitrary)
  )

  implicit def arbitraryFragment[A: Cogen]: Arbitrary[Fragment[A]] = Arbitrary(implicitly[Arbitrary[A => Option[String]]].arbitrary.map(f => f(_)))
  implicit def arbitraryPathPart[A: Cogen]: Arbitrary[PathPart[A]] = Arbitrary(implicitly[Arbitrary[A => String]].arbitrary.map(f => f(_)))
  implicit def arbitraryQueryValue[A: Cogen]: Arbitrary[QueryValue[A]] = Arbitrary(implicitly[Arbitrary[A => Option[String]]].arbitrary.map(f => f(_)))
  implicit def arbitraryQueryKey[A: Cogen]: Arbitrary[QueryKey[A]] = Arbitrary(implicitly[Arbitrary[A => String]].arbitrary.map(f => f(_)))
  implicit def arbitraryTraversableParams[A: Cogen]: Arbitrary[TraversableParams[A]] =
    Arbitrary(implicitly[Arbitrary[A => List[(String, Option[String])]]].arbitrary.map(f => f(_)))

  implicit def uriCogen[T <: Uri: Show]: Cogen[T] =
    Cogen[String].contramap(_.show)

  implicit def hostCogen[T <: Host: Show]: Cogen[T] =
    Cogen[String].contramap(_.show)

  implicit def pathCogen[T <: Path: Show]: Cogen[T] =
    Cogen[String].contramap(_.show)

  implicit val userInfoCogen: Cogen[UserInfo] =
    Cogen[String].contramap(_.show)

  implicit val queryStringCogen: Cogen[QueryString] =
    Cogen[String].contramap(_.show)

  implicit val authorityCogen: Cogen[Authority] =
    Cogen[String].contramap(_.show)

  implicit val mediaTypeCogen: Cogen[MediaType] =
    Cogen[String].contramap(_.show)
}
