package io.lemonlabs.uri

import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.decoding.NoopDecoder
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SerializableTests extends AnyFlatSpec with Matchers {
  private[this] def serializeAndDeserialize[A <: Serializable](a: A): A = {
    val bytes = new ByteArrayOutputStream
    new ObjectOutputStream(bytes).writeObject(a)

    val obj = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray)).readObject

    obj.asInstanceOf[A]
  }

  it should "uri can serializable" in {
    val uri = Uri.parse("http://example.com/path/?key=value#fragment")

    serializeAndDeserialize(uri) should equal(uri)
  }

  it should "uri can serializable, decoder = NoopDecoder" in {
    val uri = Uri.parse("http://example.com/path/?key=value#fragment")(UriConfig(decoder = NoopDecoder))

    serializeAndDeserialize(uri) should equal(uri)
  }
}
