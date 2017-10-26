package org.hablapps.puretest

import cats.Monad

trait Filter[F[_]] {
  def filter[A](fa: F[A])(f: A => Boolean)(implicit
    F: sourcecode.File,
    L: sourcecode.Line): F[A]
}

object Filter {

  def apply[F[_]](implicit S: Filter[F]) = S

  object syntax extends Syntax // scalastyle:ignore
  trait Syntax {
    implicit class FilterOps[F[_],A](fa: F[A])(implicit SF: Filter[F]){
      def filter(f: A => Boolean)(implicit F: sourcecode.File, L: sourcecode.Line): F[A] =
        SF.filter(fa)(f)
      def withFilter(f: A => Boolean)(implicit F: sourcecode.File, L: sourcecode.Line): F[A] =
        filter(f)
    }
  }

  implicit def filterForMonadError[F[_], E](implicit
      M: Monad[F],
      HE: HandleError[F, E],
      RE: RaiseError[F, PuretestError[E]]) =
    new Filter[F] {
      def filter[A](fa: F[A])(f: A => Boolean)(implicit
        F: sourcecode.File, L: sourcecode.Line): F[A] =
        fa shouldMatch f
    }

}
