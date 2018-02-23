package com.netaporter.uri

import org.scalatest.{FlatSpec, Matchers}

class HostTests extends FlatSpec with Matchers {

  "IpV4" should "convert Ints to octets and back to Ints" in {
    val ip = IpV4(100, 255, 0, 1)
    ip.octet1Int should equal(100)
    ip.octet2Int should equal(255)
    ip.octet3Int should equal(0)
    ip.octet4Int should equal(1)
  }

  it should "not allow Ints less than 0" in {
    a[IllegalArgumentException] should be thrownBy IpV4(-1, 0, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV4(0, -1, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV4(0, 0, -1, 0)
    a[IllegalArgumentException] should be thrownBy IpV4(0, 0, 0, -1)
  }

  it should "not allow Ints greater than 255" in {
    a[IllegalArgumentException] should be thrownBy IpV4(256, 0, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV4(0, 256, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV4(0, 0, 256, 0)
    a[IllegalArgumentException] should be thrownBy IpV4(0, 0, 0, 256)
  }

  it should "toString correctly" in {
    val ip = IpV4(100, 255, 0, 1)
    ip.toString should equal("100.255.0.1")
  }

  "IpV6" should "convert Ints to Char pieces and back to Ints" in {
    val ip = IpV6(0, 65535, 10, 20, 30, 40, 50, 60)
    ip.piece1Int should equal(0)
    ip.piece2Int should equal(65535)
    ip.piece3Int should equal(10)
    ip.piece4Int should equal(20)
    ip.piece5Int should equal(30)
    ip.piece6Int should equal(40)
    ip.piece7Int should equal(50)
    ip.piece8Int should equal(60)
  }

  it should "convert Hex pieces to Char pieces and back to hex" in {
    val ip = IpV6("0", "ffff", "10", "20", "30", "40", "50", "60")
    ip.toString should equal("[0:ffff:10:20:30:40:50:60]")
  }

  it should "not allow Ints less than 0" in {
    a[IllegalArgumentException] should be thrownBy IpV6(-1, 0, 0, 0, 0, 0, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, -1, 0, 0, 0, 0, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 0, -1, 0, 0, 0, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 0, 0, -1, 0, 0, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 0, 0, 0, -1, 0, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 0, 0, 0, 0, -1, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 0, 0, 0, 0, 0, -1, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 0, 0, 0, 0, 0, 0, -1)
  }

  it should "not allow Ints greater than 65535" in {
    a[IllegalArgumentException] should be thrownBy IpV6(65536, 0, 0, 0, 0, 0, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 65536, 0, 0, 0, 0, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 0, 65536, 0, 0, 0, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 0, 0, 65536, 0, 0, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 0, 0, 0, 65536, 0, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 0, 0, 0, 0, 65536, 0, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 0, 0, 0, 0, 0, 65536, 0)
    a[IllegalArgumentException] should be thrownBy IpV6(0, 0, 0, 0, 0, 0, 0, 65536)
  }

  it should "toString localhost" in {
    val ip = IpV6(0, 0, 0, 0, 0, 0, 0, 1)
    ip.toString should equal("[::1]")
    ip.toStringNonNormalised should equal("[0:0:0:0:0:0:0:1]")
  }

  it should "collapse the longest series of 0s in toString" in {
    IpV6(500, 0, 0, 30, 0, 0, 0, 1).toString should equal("[1f4:0:0:1e::1]")
    IpV6(500, 0, 0, 0, 30, 0, 0, 1).toString should equal("[1f4::1e:0:0:1]")
  }
}
