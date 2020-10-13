package io.lemonlabs.uri

import java.awt.Color
import java.io.ByteArrayInputStream

import javax.imageio.ImageIO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DataUrlJvmTests extends AnyFlatSpec with Matchers {
  "publicSuffixes and subdomains" should "be empty" in {
    val dataUrl = DataUrl.parse("data:,A%20brief%20note")
    dataUrl.publicSuffix should equal(None)
    dataUrl.publicSuffixes should equal(Vector.empty)
    dataUrl.subdomain should equal(None)
    dataUrl.subdomains should equal(Vector.empty)
    dataUrl.shortestSubdomain should equal(None)
    dataUrl.longestSubdomain should equal(None)
  }

  /** From https://en.wikipedia.org/wiki/Data_URI_scheme#HTML
    */
  "The DataUrl for a red dot PNG image" should "have the correct pixel values in Java BufferedImage" in {
    val dataUrl = DataUrl.parse(
      "data:image/png;base64,iVBORw0KGgoAAA\nANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4\n//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU\n5ErkJggg=="
    )
    val image = ImageIO.read(new ByteArrayInputStream(dataUrl.data))

    // Image is a red dot, transparent in the top left corner of image
    val firstPixel = new Color(image.getRGB(0, 0), true)
    firstPixel.getRed should equal(255)
    firstPixel.getGreen should equal(255)
    firstPixel.getBlue should equal(255)
    firstPixel.getAlpha should equal(0)

    // Image is a red dot, red in the centre of the image
    val middlePixel = new Color(image.getRGB(2, 2), true)
    middlePixel.getRed should equal(255)
    middlePixel.getGreen should equal(0)
    middlePixel.getBlue should equal(0)
    middlePixel.getAlpha should equal(255)
  }
}
