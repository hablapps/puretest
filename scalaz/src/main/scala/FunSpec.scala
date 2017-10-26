package org.hablapps.puretest

trait FunSpec[P[_],E] {
  implicit val HE: HandleError[P, E]
  implicit val RE: RaiseError[P, PuretestError[E]]

  def Describe(subject: String)(test: => Unit): Unit

  def It[A](condition: String)(program: => P[A]): Unit

  def Holds(condition: String)(program: => P[Boolean]): Unit
}

