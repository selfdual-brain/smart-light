package com.selfdualbrain.finatrapoc.core.model

sealed trait OnOffStatus {
}

object OnOffStatus {
  case object On extends OnOffStatus
  case object Off extends OnOffStatus
}

