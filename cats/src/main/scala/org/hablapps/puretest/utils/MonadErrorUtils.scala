package org.hablapps.puretest

import cats.MonadError

trait MonadErrorUtils {

  trait RaiseError[P[_],E]{
    def raiseError[A](e: E): P[A]
  }

  object RaiseError{
    def apply[P[_],E](implicit RE: RaiseError[P,E]) = RE

    implicit def fromMonadError[P[_], E](implicit ME: MonadError[P, E]) =
      new RaiseError[P, E]{
        def raiseError[A](e: E) = ME.raiseError(e)
      }
  }

  trait HandleError[P[_],E]{
    def handleError[A](p: P[A])(f: E => P[A]): P[A]
  }

  object HandleError{
    def apply[P[_],E](implicit HE: HandleError[P,E]) = HE

    implicit def fromMonadError[P[_],E](implicit ME: MonadError[P,E]) =
      new HandleError[P,E]{
        def handleError[A](p: P[A])(f: E => P[A]) = ME.handleErrorWith(p)(f)
      }
  }

}
