package com.github.theon.uri

import com.github.theon.uri.config.UriConfig
import Parameters.ParamSeq

/**
 * Date: 28/08/2013
 * Time: 21:22
 */
case class QueryString(params: ParamSeq) extends Parameters[QueryString] {

  def separator = "&"

  def withParams(params: ParamSeq) = copy(params = params)

  def queryToString(c: UriConfig) =
    if(params.isEmpty) {
      ""
    } else {
      "?" + paramsToString(c.queryEncoder, c.charset)
    }
}

object EmptyQueryString extends QueryString(Seq.empty)