package org.hablapps.puretest
package scalatestImpl

import org.scalatest._, matchers._, Matchers._

trait ProgramStateMatchers[P[_], S, E]{
  def apply(from: S): ProgramMatchers[P, E]
}

object ProgramStateMatchers{
  def apply[P[_], S, E](implicit PSM: ProgramStateMatchers[P, S, E]) = PSM

  class FromBuilder[P[_], S](from: S) {
    def failWith[E](error: E)(implicit PSM: ProgramStateMatchers[P, S, E]) =
      PSM(from).failWith(error)
    def beEqualTo[A](value: A)(implicit PSM: ProgramStateMatchers[P, S, _]) =
      PSM(from).beEqualTo(value)
    def beSatisfied(implicit PSM: ProgramStateMatchers[P, S, _]) =
      PSM(from).beSatisfied
    def runWithoutErrors(implicit PSM: ProgramStateMatchers[P, S, _]) =
      PSM(from).runWithoutErrors
  }

  trait FromAux[P[_]] {
    def apply[S](from: S) = new FromBuilder[P, S](from)
  }

  trait Syntax{
    def from[P[_]] = new FromAux[P] {}
  }

  object Syntax extends Syntax

  implicit def matcher[P[_], S, E](implicit tester: StateTester[P, S, E]) =
    new ProgramStateMatchers[P, S, E]{
      def apply(from: S) =
        ProgramMatchers.matcher(tester(from))
    }
}
