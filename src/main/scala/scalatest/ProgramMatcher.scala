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
      import scalaz.\/

      def beSatisfied = new Matcher[P[Boolean]]{
        def apply(program: P[Boolean]) = {
          val evaluated: E \/ Boolean = test(program)
          MatchResult(
            evaluated.fold(_ => false, identity),
            evaluated.fold(
              error => s"Unexpected error $error",
              _ => "Boolean program returned false"),
            evaluated.fold(
              _ => "should not happen",
              _ => s"Boolean program returned true"))
        }
      }

      def runWithoutErrors = new Matcher[P[_]]{
        def apply(program: P[_]) = {
          val evaluated = test(program)
          MatchResult(
            evaluated.isRight,
            evaluated.fold(
              { case e: Throwable => "Unexpected exception " + e.toString + "\n" + e.getStackTrace.mkString("\n")
                case e => "Unexpected exception " + e.toString },
              _ => "should not happen"),
            evaluated.fold(
              _ => "should not happen",
              result => s"Unexpected returned value $result"))
        }
      }
    }
}
