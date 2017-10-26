package org.hablapps.puretest

import cats.MonadError

/**
 * Puretest errors
 */

sealed abstract class PuretestError[E](msg: String) {
  override def toString = msg
}

object PuretestError extends PuretestErrorImplicits {
  def simplifyLocation(location: Location): String = {
    val fileext = raw".*/(.*)".r
    val fileext(filename) = location._1.value
    s"($filename:${location._2.value})"
  }
}

trait PuretestErrorImplicits {
  implicit def toMonadError[P[_], E](implicit
      ME: MonadError[P, PuretestError[E]]) =
    new MonadError[P, E] {
      def pure[A](a: A) = ME.pure(a)
      def flatMap[A,B](p: P[A])(f: A => P[B]) = ME.flatMap(p)(f)
      def tailRecM[A, B](a: A)(f: A => P[Either[A, B]]): P[B] = ME.tailRecM(a)(f)
      def raiseError[A](e: E) = ME.raiseError(ApplicationError(e))
      def handleErrorWith[A](fa: P[A])(f: E => P[A]): P[A] = ME.recoverWith(fa) {
        case ApplicationError(e) => f(e)
      }
    }
}

import PuretestError.simplifyLocation

case class ApplicationError[E](e: E) extends PuretestError[E](s"Application error: $e")

case class NotEqualTo[E,A](found: A, expected: A)(implicit location: Location)
  extends PuretestError[E](s"Value $expected expected but found value $found ${simplifyLocation(location)}")

case class NotFailed[E,A](found: A)(implicit location: Location)
  extends PuretestError[E](s"Error expected but found value $found ${simplifyLocation(location)}")

case class NotSucceeded[E](found: E)(implicit location: Location)
  extends PuretestError[E](s"Value expected but found error $found ${simplifyLocation(location)}")

case class NotError[A, E](found: A, expected: E)(implicit location: Location)
  extends PuretestError[E](s"Error $expected expected but found value $found ${simplifyLocation(location)}")

case class NotValue[A, E](found: E, expected: A)(implicit location: Location)
  extends PuretestError[E](s"Value $expected expected but found error $found ${simplifyLocation(location)}")

case class OtherError[E](found: E, expected: E)(implicit location: Location)
  extends PuretestError[E](s"Error $expected expected but found error $found ${simplifyLocation(location)}")

case class NotMatched[A,E](found: A)(implicit location: Location)
  extends PuretestError[E](s"Expected pattern doesn't match found value $found ${simplifyLocation(location)}")

case class NotMatchedFailure[E](found: E)(implicit location: Location)
  extends PuretestError[E](s"Expected pattern doesn't match found error $found ${simplifyLocation(location)}")

case class ShouldNotHappen[E](implicit location: Location)
  extends PuretestError[E](s"This error shouldn't ever be thrown ${simplifyLocation(location)}")
