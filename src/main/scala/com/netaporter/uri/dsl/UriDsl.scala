package com.netaporter.uri.dsl

import com.netaporter.uri.{Uri, StringPathPart}

/**
 * Value class to add DSL functionality to Uris
 *
 * @param uri
 */
class UriDsl(val uri: Uri) extends AnyVal {

  /**
   * Adds a new Query String parameter key-value pair. If the value for the Query String parameter is None, then this
   * Query String parameter will not be rendered in calls to toString or toStringRaw
   * @param kv Tuple2 representing the query string parameter
   * @return A new Uri with the new Query String parameter
   */
  def ?(kv: (String, Any)) = uri.addParam(kv._1, kv._2)

  /**
   * Adds a new Query String parameter key-value pair. If the value for the Query String parameter is None, then this
   * Query String parameter will not be rendered in calls to toString or toStringRaw
   * @param kv Tuple2 representing the query string parameter
   * @return A new Uri with the new Query String parameter
   */
  def &(kv: (String, Any)) = uri.addParam(kv._1, kv._2)


  /**
   * Adds a fragment to the end of the uri
   * @param fragment String representing the fragment
   * @return A new Uri with this fragment
   */
  def `#`(fragment: String) = uri.withFragment(fragment)


  /**
   * Appends a path part to the path of this URI
   * @param pp The path part
   * @return A new Uri with this path part appended
   */
  def /(pp: String) = uri.copy(pathParts = uri.pathParts :+ StringPathPart(pp))
}
