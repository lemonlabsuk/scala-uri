package io.lemonlabs.uri.inet

import scala.scalanative.unsafe.*

@link("idn2")
@extern
private[inet] object CIdn {
  @name("idn2_to_ascii_8z")
  def toAscii(input: CString, output: Ptr[CString], flags: CInt): CInt = extern

  @name("idn2_strerror")
  def errorMsg(rc: CInt): CString = extern

  @name("idn2_free")
  def free(ptr: Ptr[_]): Unit = extern

}

trait PunycodeSupport {
  def toPunycode(host: String): String =
    Zone.acquire { implicit z: Zone =>
      import scalanative.runtime.ffi.*
      val output: Ptr[CString] = alloc[CString]()
      var rc = CIdn.toAscii(toCString(host), output, IDN2_NONTRANSITIONAL)

      if (rc == IDN2_DISALLOWED) {
        rc = CIdn.toAscii(toCString(host), output, IDN2_TRANSITIONAL)
      }

      if (rc != 0) {
        val errMsg = CIdn.errorMsg(rc)
        throw new IllegalArgumentException(fromCString(errMsg))
      } else {
        val out = fromCString(!output)
        CIdn.free(!output)
        out
      }
    }

  private val IDN2_TRANSITIONAL = 4
  private val IDN2_NONTRANSITIONAL = 8
  private val IDN2_DISALLOWED = -304
}
