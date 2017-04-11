package org.hablapps.puretest

import org.scalatest._, matchers._, Matchers._

/**
 * Program matcher
 */
trait ProgramMatchers[P[_]]{
  def beSatisfied: Matcher[P[Boolean]]
}

object ProgramMatchers{

  def apply[P[_]](implicit PM: ProgramMatchers[P]) = PM

  trait Syntax{
    def beSatisfied[P[_]](implicit S1: ProgramMatchers[P]) =
      S1.beSatisfied
  }

  object Syntax extends Syntax

  implicit def matcher[P[_], E](implicit test: Tester[P,E]) =
    new ProgramMatchers[P]{
      def beSatisfied = new Matcher[P[Boolean]]{
        def apply(program: P[Boolean]) = {
          val evaluated = test(program)
          MatchResult(
            evaluated.fold(_ => false, identity),
            evaluated.fold(_.toString, r => "should not happen"),
            evaluated.fold(_.toString, r => "should not happen"))
        }
      }
    }
}
