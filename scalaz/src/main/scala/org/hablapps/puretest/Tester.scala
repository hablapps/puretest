package org.hablapps.puretest

import scalaz.{\/, ~>}

/**
 * Testers
 */
trait Tester[P[_], E] extends (P ~> Either[E, ?])

object Tester {
  def apply[P[_], E](implicit T: Tester[P, E]) = T

  /* Testing either programs */

  implicit def disjunctionTester[E] =
    new Tester[E \/ ?, E] {
      def apply[X](e: E \/ X) = e.toEither
    }

  implicit def eitherTester[E] =
    new Tester[Either[E, ?], E] {
      def apply[X](e: Either[E, X]) = e
    }

  /* Testing asynch programs */

  import scala.concurrent.{Await, Future, duration}, duration._
  import scala.util.Try
  import scala.util.{Success, Failure}

  implicit def futureTester =
    new Tester[Future, Throwable] {
      def apply[X](f: Future[X]) =
        Try(Await.result(f, 60 second)) match {
          case Success(s) => Right(s)
          case Failure(t) => Left(t)
        }
    }
}
