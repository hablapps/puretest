package org.hablapps.puretest

import cats.~>

/**
 * Testers
 */
trait Tester[P[_], E] extends (P ~> Either[E, ?])

object Tester{
  def apply[P[_], E](implicit T: Tester[P, E]) = T

  /* Testing either programs */

  implicit def eitherTester[E]: Tester[Either[E, ?], E] =
    new Tester[Either[E, ?], E]{
      def apply[X](e: Either[E, X]) = e
    }

  /* Testing asynch programs */

  import scala.concurrent.{Await, Future, duration}, duration._
  import scala.util.Try
  import scala.util.{Success, Failure}

  implicit def futureTester: Tester[Future, Throwable] =
    new Tester[Future,Throwable]{
      def apply[X](f: Future[X]) =
        (Try(Await.result(f, 60 second)) match {
          case s@Success(_) => s
          case f@Failure(t) => f
        }) match { // TODO(jfuentes): In scala 2.11 there is no `toEither` method
          case Failure(t) => Left(t)
          case Success(s) => Right(s)
        }
    }
}
