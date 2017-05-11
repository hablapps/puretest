package org.hablapps.puretest
package scalatestImpl

import org.scalatest._, matchers._, Matchers._

/**
 * Program matcher
 */
trait ProgramMatchers[P[_], E]{
  def failWith(error: E): Matcher[P[_]]
  def beEqualTo[A](value: A): Matcher[P[A]]
  def beSatisfied: Matcher[P[Boolean]]
  def runWithoutErrors: Matcher[P[_]]
}

object ProgramMatchers{
  def apply[P[_], E](implicit PM: ProgramMatchers[P, E]) = PM

  trait FailWithAux[P[_]] {
    def apply[E](error: E)(implicit PM: ProgramMatchers[P, E]) =
      PM.failWith(error)
  }

  trait BeEqualToAux[P[_]] {
    def apply[A](value: A)(implicit PM: ProgramMatchers[P, _]) =
      PM.beEqualTo(value)
  }

  trait Syntax{
    def failWith[P[_]] = new FailWithAux[P] {}
    def beEqualTo[P[_]] = new BeEqualToAux[P] {}
    def beSatisfied[P[_]](implicit PM: ProgramMatchers[P, _]) =
      PM.beSatisfied
    def runWithoutErrors[P[_]](implicit PM: ProgramMatchers[P, _]) =
      PM.runWithoutErrors
  }

  object Syntax extends Syntax

  implicit def matcher[P[_], E](implicit test: Tester[P, E]) =
    new ProgramMatchers[P, E]{
      def failWith(error: E) = new Matcher[P[_]] {
        def apply(program: P[_]) =
          test(program).fold(
            e => MatchResult(e == error, s"Expected: $error ; Found: $e", "Result matches expected error"),
            a => MatchResult(false, s"Unexpected successful result: $a", "should not happen"))
      }

      def beEqualTo[A](value: A) = new Matcher[P[A]] {
        def apply(program: P[A]) =
          test(program).fold(
            error => MatchResult(false, s"Expected: $value ; Error Found: $error", "should not happen"),
            a => MatchResult(a == value, s"Expected: $value ; Found: $a", "Result matches expected"))
      }

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
