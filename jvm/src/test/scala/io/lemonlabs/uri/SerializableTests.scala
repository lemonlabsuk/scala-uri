package io.lemonlabs.uri

import org.scalatest.{FlatSpec, Matchers}
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.decoding.NoopDecoder

class SerializableTests extends FlatSpec with Matchers {

  private[this] def serializeAndDeserialize[A <: Serializable](a: A): A = {
    val bytes = new ByteArrayOutputStream
    new ObjectOutputStream(bytes).writeObject(a)

    val obj = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray)).readObject

    obj.asInstanceOf[A]
  }

  it should "uri can serializable" in {
    val uri = Uri.parse("http://example.com/path/?key=value#flagment")

    serializeAndDeserialize(uri) should equal(uri)
  }

  it should "uri can serializable, decoder = NoopDecoder" in {
    val uri = Uri.parse("http://example.com/path/?key=value#flagment")(UriConfig(decoder = NoopDecoder))

    serializeAndDeserialize(uri) should equal(uri)
  }
}
