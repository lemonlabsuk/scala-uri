package benchmarks

import org.scalameter.ScalaMeterFramework
import org.scalatools.testing._

/**
 * Having both ScalaTest Unit Tests and ScalaMeter Tests in the same project means that running `sbt test` would run
 * both sets of tests. We don't want performance tests running on travis and was unable to only run unit tests with
 * `test-only` as scct doesn't support that (https://github.com/mtkopone/scct/issues/48), so this class wraps the
 * ScalaMeterFramework and first checks for an argument `-Cbenchmarking true` and only runs the benchmarking tests
 * if this argument is present.
 */
class OnOffScalaMeterFramework extends Framework {

  val delegate = new ScalaMeterFramework()

  override def name = delegate.name()
  override def tests = delegate.tests()

  def testRunner(testClassLoader: ClassLoader, loggers: Array[Logger]) = {
    val superRunner = delegate.testRunner(testClassLoader, loggers)
    new Runner2 {
      def run(testClassName: String, fingerprint: Fingerprint, eventHandler: EventHandler, args: Array[String]) {
        val enabled = args.mkString(" ").contains("-Cbenchmarking true")
        if(enabled) {
          val newArgs = Array(args.mkString(" ").replaceAll("\\-Cbenchmarking (true|false)", ""))
          superRunner.run(testClassName, fingerprint, eventHandler, newArgs)
        }
      }
    }
  }
}
