package org.hablapps.puretest
package scalatestImpl

import org.scalatest._, matchers._

/**
 * Program matcher
 */
trait ProgramMatchers[P[_]]{
  def beSatisfied: Matcher[P[Boolean]]
  def runWithoutErrors: Matcher[P[_]]
}

object ProgramMatchers{

  def apply[P[_]](implicit PM: ProgramMatchers[P]) = PM

  trait Syntax{
    def beSatisfied[P[_]](implicit S1: ProgramMatchers[P]) =
      S1.beSatisfied
    def runWithoutErrors[P[_]](implicit PM: ProgramMatchers[P]) =
      PM.runWithoutErrors
  }

  object Syntax extends Syntax

  implicit def matcher[P[_], E](implicit test: Tester[P, E]) =
    new ProgramMatchers[P]{

      def beSatisfied = new Matcher[P[Boolean]]{
        def apply(program: P[Boolean]) =
          test(program).fold(
            error => MatchResult(false, s"$error", "should not happen"),
            b => MatchResult(b, "Boolean program returned false", "Boolean program returned true"))
      }

      def runWithoutErrors = new Matcher[P[_]]{
        def apply(program: P[_]) =
          test(program).fold(
            error => MatchResult(false, error.toString, "should not happen"),
            result => MatchResult(true, "should not happen", s"Program ran w/o errors: $result"))
      }
    }
}
