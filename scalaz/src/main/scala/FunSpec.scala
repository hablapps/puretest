package org.hablapps.puretest

trait FunSpec[P[_]] {
  def Describe(subject: String)(test: => Unit): Unit

  def It[A](condition: String)(program: => P[A]): Unit

  def Holds(condition: String)(program: => P[Boolean]): Unit
}

