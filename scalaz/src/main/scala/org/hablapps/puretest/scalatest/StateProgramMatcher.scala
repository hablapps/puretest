package org.hablapps.puretest
package scalatestImpl


trait ProgramStateMatchers[P[_],S]{
  def apply(from: S): ProgramMatchers[P]
}

object ProgramStateMatchers{

  def apply[P[_],S](implicit PM: ProgramStateMatchers[P,S]) = PM

  trait Syntax{
    def beSatisfied[P[_],S](from: S)(implicit S1: ProgramStateMatchers[P,S]) =
      S1(from).beSatisfied
    def runWithoutErrors[P[_],S](from: S)(implicit PM: ProgramStateMatchers[P,S]) = 
      PM(from).runWithoutErrors
  }

  object Syntax extends Syntax

  implicit def matcher[P[_], S, E](implicit tester: StateTester[P,S,E]) =
    new ProgramStateMatchers[P,S]{
      def apply(from: S) = 
        ProgramMatchers.matcher(tester(from))
    }
}
