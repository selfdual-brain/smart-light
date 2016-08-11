package com.selfdualbrain.finatrapoc.core.model

trait ConnectionStatus {

}

object ConnectionStatus {
  case object Alive extends ConnectionStatus
  case object Lost extends ConnectionStatus
}
