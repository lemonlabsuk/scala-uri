# scala-uri

[![Build Status](https://secure.travis-ci.org/theon/scala-uri.png?branch=master)](https://travis-ci.org/theon/scala-uri)

`scala-uri` is a small self contained Scala library that helps you work with URIs. It can be used outside a servlet environment as it has zero dependencies on the servlet spec or existing web frameworks.

## Including in your project

TODO: Put in repo. Add SBT config

## Building URIs with the DSL

    import com.github.theon.uri.Uri._
    val uri = "http://theon.github.com/scala-uri" ? ("param1" -> "1") & ("param2" -> "2")

## Parsing URIs

    import com.github.theon.uri.Uri._
    val uri = parseUri("http://theon.github.com/scala-uri?param1=1&param2=2")

# License

`scala-uri` is open source software released under the [Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0).