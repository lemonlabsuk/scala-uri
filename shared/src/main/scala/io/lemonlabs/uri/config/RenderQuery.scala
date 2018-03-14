package io.lemonlabs.uri.config

object RenderQuery {
  def default: RenderQuery = All
}

sealed trait RenderQuery
case object All extends RenderQuery
case object ExcludeNones extends RenderQuery