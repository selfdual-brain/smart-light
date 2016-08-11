package com.selfdualbrain.finatrapoc.core.model

/**
  * Abstract base class for light colors.
  * Subclasses correcpond to different color spaces.
  *
  * Caution: this representation of color does not include brighness. So we say "color" but we think "color shade".
  */
sealed trait Color {
  def convertToCie: CieColorPoint
  def convertToHSV: HSV
  def convertToColorTemp: ColorTemp
}

//caution: conversion algorithms below are fake - this is just for providing example functionality

case class HSV(hue: Int, saturation: Int) extends Color {
  override def convertToCie: CieColorPoint = CieColorPoint(hue.toDouble * saturation / (2*math.Pi), hue.toDouble / (saturation + 1))
  override def convertToHSV: HSV = this
  override def convertToColorTemp: ColorTemp = ColorTemp(1250230 / (hue * saturation + 1))
}

case class CieColorPoint(x: Double, y: Double) extends Color {
  override def convertToCie: CieColorPoint = this
  override def convertToColorTemp: ColorTemp = ColorTemp((1250230/(x * y + 1)).toInt)
  override def convertToHSV: HSV = HSV((x*251).toInt, (y*math.Pi).toInt)
}

case class ColorTemp(t: Int) extends Color {
  override def convertToCie: CieColorPoint = CieColorPoint(math.sin(t) * 1800, math.Pi * t + 122)
  override def convertToColorTemp: ColorTemp = this
  override def convertToHSV: HSV = HSV((math.sqrt(t*math.Pi) + 23).toInt, (math.Pi * t).toInt)
}
