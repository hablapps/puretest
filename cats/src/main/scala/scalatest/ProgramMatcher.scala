package org.hablapps.puretest

import org.scalatest._, matchers._, Matchers._

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

  implicit def matcher[P[_], E](implicit test: Tester[P,E]) =
    new ProgramMatchers[P]{

      def beSatisfied = new Matcher[P[Boolean]]{
        def apply(program: P[Boolean]) =
          test(program).fold(
            error => MatchResult(false, s"Unexpected error $error", "should not happen"),
            b => MatchResult(b, "Boolean program returned false", "Boolean program returned true"))
      }

      def runWithoutErrors = new Matcher[P[_]]{
        def apply(program: P[_]) =
          test(program).fold(
            error => {
              val msg = error match { // TODO: This is ugly
                case e: Throwable => s"Unexpected exception $e\n" + e.getStackTrace.mkString("\n")
                case e => s"Unexpected exception $e"
              }
              MatchResult(false, msg, "should not happen")
            },
            result => MatchResult(true, "should not happen", s"Program ran w/o errors: $result"))
      }
    }
}
