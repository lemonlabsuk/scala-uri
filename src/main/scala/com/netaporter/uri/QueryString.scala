package com.netaporter.uri

import com.netaporter.uri.config.UriConfig
import com.netaporter.uri.Parameters.ParamSeq

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