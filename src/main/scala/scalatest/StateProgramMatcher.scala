package org.hablapps.puretest
package scalatestImpl

import org.scalatest._, matchers._, Matchers._

trait ProgramStateMatchers[P[_],S]{
  def beSatisfied(from: S): Matcher[P[Boolean]]
}

object ProgramStateMatchers{

  def apply[P[_],S](implicit PM: ProgramStateMatchers[P,S]) = PM

  trait Syntax{
    def beSatisfied[P[_],S](from: S)(implicit S1: ProgramStateMatchers[P,S]) =
      S1.beSatisfied(from)
  }

  object Syntax extends Syntax

  implicit def matcher[P[_], S, E](implicit tester: StateTester[P,S,E]) =
    new ProgramStateMatchers[P,S]{
      def beSatisfied(from: S) = 
        ProgramMatchers.matcher(tester(from)).beSatisfied
    }
}
