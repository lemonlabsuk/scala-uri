package com.github.theon.uri

import com.github.theon.uri.config.UriConfig
import com.github.theon.uri.Parameters.{Param, ParamSeq}

/**
 * Date: 28/08/2013
 * Time: 21:22
 */
case class QueryString(params: ParamSeq) extends Parameters {

  type Self = QueryString

  def separator = "&"

  def withParams(paramsIn: ParamSeq) =
    QueryString(paramsIn)

  def queryToString(c: UriConfig) =
    if(params.isEmpty) ""
    else "?" + paramsToString(c.queryEncoder, c.charset)
}

object EmptyQueryString extends QueryString(Seq.empty)