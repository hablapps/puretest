package org.hablapps.puretest

import scalaz.MonadError, scalaz.syntax.monadError._

trait MonadErrorUtils{

  trait RaiseError[P[_],E]{
    def raiseError[A](e: E): P[A]
  }

  object RaiseError{
    def apply[P[_],E](implicit RE: RaiseError[P,E]) = RE

    implicit def fromMonadError[P[_],E](implicit ME: MonadError[P,E]) = 
      new RaiseError[P,E]{
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
        def handleError[A](p: P[A])(f: E => P[A]) = ME.handleError(p)(f)
      }
  }

  implicit class MonadErrorOps[P[_],A](self: P[A]){
    def attempt[E: MonadError[P, ?]]: P[Either[E, A]] =
      (self map (Right(_): Either[E, A]))
        .handleError(error => (Left(error): Either[E,A]).pure[P])

    def recoverWith[E: MonadError[P,?]](pf: PartialFunction[E,P[A]]): P[A] = 
      self.handleError{ e => 
        pf applyOrElse(e, (_: E).raiseError[P,A])
      }
  }
}