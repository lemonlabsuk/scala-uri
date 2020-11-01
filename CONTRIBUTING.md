# Contributions

Contributions to `scala-uri` are always welcome. Good ways to contribute include:

 * Raising bugs and feature requests
 * Fixing bugs and developing new features (I will attempt to merge in pull requests ASAP)
 * Improving the performance of `scala-uri`. See the [Performance Tests](https://github.com/lemonlabsuk/scala-uri-benchmarks) project for details of how to run the `scala-uri` performance benchmarks.
 
Keep your change as focused as possible. If there are multiple changes you would like to make that are not dependent upon each other, submit them as separate pull requests :pray:

# Building scala-uri

## Unit Tests

The unit tests can be run from the sbt console by running the `test` command! Checking the unit tests all pass before sending pull requests will be much appreciated.

Generate code coverage reports from the sbt console by running `sbt coverage test coverageReport`. Ideally pull requests shouldn't significantly decrease code coverage, but it's not the end of the world if they do. Contributions with no tests are better than no contributions :)

## Performance Tests

For the `scala-uri` performance tests head to the [scala-uri-benchmarks](https://github.com/lemonlabsuk/scala-uri-benchmarks) github project
