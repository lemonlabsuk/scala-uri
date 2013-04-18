package benchmarks

import org.scalameter._
import org.scalameter.execution.SeparateJvmsExecutor
import org.scalameter.Executor.Measurer
import org.scalameter.reporting.HtmlReporter
import org.scalameter.reporting.ChartReporter.ChartFactory
import util.Random
import com.github.theon.uri.Uri._
import com.github.theon.uri.{Querystring, Uri}

/**
 * Date: 14/04/2013
 * Time: 18:29
 */
object RenderingBenchmark extends PerformanceTest {

  lazy val executor = SeparateJvmsExecutor(
    Executor.Warmer.Default(),
    Aggregator.average,
    new Measurer.IgnoringGC
  )
  //lazy val reporter = new LoggingReporter
  //lazy val reporter = ChartReporter(ChartFactory.XYLine())
  lazy val reporter = HtmlReporter(HtmlReporter.Renderer.Info(), HtmlReporter.Renderer.BigO(), HtmlReporter.Renderer.Chart(ChartFactory.XYLine()))
  lazy val persistor = Persistor.None

  val lengths = Gen.range("size")(1, 100000, 10000)
  val testData = lengths.map(i => Random.alphanumeric.take(i).mkString)

  val testLongPaths = testData.map(data => Uri("/" + data))
  val testLongDomains = testData.map(data => Uri("http", data, ""))
  val testLongQueryKeys = testData.map(data => Uri("/", Querystring(Map(data -> List("value")))))
  val testLongQueryValues = testData.map(data => Uri("/", Querystring(Map("key" -> List(data)))))

  performance of "Uri Rendering" config (api.exec.benchRuns -> 36, api.exec.maxWarmupRuns -> 10) in {

    measure method "path length" in {
      using(testLongPaths) in {
        uri => uri.toString
      }
    }

    measure method "domain length" in {
      using(testLongDomains) in {
        uri => uri.toString
      }
    }

    measure method "query string key length" in {
      using(testLongQueryKeys) in {
        uri => uri.toString
      }
    }

    measure method "query string value length" in {
      using(testLongQueryValues) in {
        uri => uri.toString
      }
    }
  }
}