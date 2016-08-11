package com.selfdualbrain.finatrapoc.core.model

sealed trait MeasuredQuantity {
}

object MeasuredQuantity {
  case object Temp extends MeasuredQuantity
  case object Humidity extends MeasuredQuantity
  case object Daylight extends MeasuredQuantity
}