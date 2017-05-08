package org.hablapps.puretest

/**
 * Utilities for test specifications
 */
trait TestingOps{

  implicit class TestingOps[P[_], A](self: P[A]){

    import scalaz.MonadError
    import scalaz.syntax.monadError._

    def isError[E: MonadError[P,?]](e: E): P[Boolean] =
      (self as false).handleError{
        error: E => (error == e).point[P]
      }

    def error[E: MonadError[P,?]]: P[Either[E,A]] =
      (self map (Right(_): Either[E,A])).handleError{
        _.point[P] map Left.apply[E,A]
      }

    import scalaz.Functor

    def isEqual(a: A)(implicit F: Functor[P]): P[Boolean] =
      F.map(self)(_ == a)

    case class NotEqualTo[A](a1: A, a2: A) extends RuntimeException

    def assertEqual(a1: A)(implicit ME: MonadError[P, Throwable]): P[Unit] =
      self >>= { a2 =>
        if (a1 == a2) ().point[P] else ME.raiseError(NotEqualTo(a1, a2))
      }
  }
}
