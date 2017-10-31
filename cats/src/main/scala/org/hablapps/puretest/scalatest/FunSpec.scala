package org.hablapps
package puretest
package scalatestImpl

trait FunSpec[P[_], E] extends org.scalatest.FunSpec
  with org.scalatest.Matchers
  with puretest.FunSpec[P] {

  implicit val Tester: Tester[P, PuretestError[E]]

  def Describe(subject: String)(test: => Unit): Unit = // scalastyle:ignore
    describe(subject)(test)

  import ProgramMatchers.syntax._

  def It[A](condition: String)(program: => P[A]): Unit = // scalastyle:ignore
    it(condition) {
      program should runWithoutErrors
    }

  def Holds(condition: String)(program: => P[Boolean]): Unit = // scalastyle:ignore
    it(condition) {
      program should beSatisfied
    }
}
