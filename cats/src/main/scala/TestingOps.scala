package org.hablapps.puretest

import cats.{ApplicativeError, Functor}
import cats.syntax.applicativeError._
import cats.syntax.functor._

/**
 * Utilities for test specifications
 */
trait TestingOps{

  implicit class TestingOps[P[_], A](self: P[A]){

    def isError[E: ApplicativeError[P,?]](e: E): P[Boolean] =
      (self as false).handleError{
        error: E => error == e
      }

    def error[E: ApplicativeError[P,?]]: P[Either[E, A]] =
      (self map (Right(_): Either[E, A])).handleError(Left.apply[E, A])

    def isEqual(a: A)(implicit F: Functor[P]): P[Boolean] =
      F.map(self)(_ == a)

  }
}

