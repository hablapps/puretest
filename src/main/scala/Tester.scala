package org.hablapps.puretest

import scalaz.{\/, ~>}

/** 
 * Testers
 */ 
trait Tester[P[_],E] extends (P~> (E \/ ?))

object Tester{
  def apply[P[_],E](implicit T: Tester[P,E]) = T

  /* Testing either programs */

  implicit def eitherTester[E]: Tester[E \/ ?, E] = 
    new Tester[E \/ ?,E]{
      def apply[X](e: E \/ X) = e
    }

  /* Testing asynch programs */

  import scala.concurrent.{Await, Future, duration}, duration._
  import scala.util.Try, scalaz.syntax.std.`try`._

  implicit def futureTester: Tester[Future, Throwable] = 
    new Tester[Future,Throwable]{
      def apply[X](f: Future[X]) =
        Try(Await.result(f, 20 second))
          .toDisjunction
    }
}
