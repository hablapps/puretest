package org.hablapps.puretest

trait Filter[F[_]]{
  def filter[A](fa: F[A])(f: A => Boolean)(implicit
    F: sourcecode.File, L: sourcecode.Line): F[A]
}

object Filter{

  def apply[F[_]](implicit S: Filter[F]) = S

  // Use in for-comprehensions
  trait Syntax{

    implicit class FilterOps[F[_],A](fa: F[A])(implicit SF: Filter[F]){
      def filter(f: A => Boolean)(implicit F: sourcecode.File, L: sourcecode.Line): F[A] =
        SF.filter(fa)(f)
      def withFilter(f: A => Boolean)(implicit F: sourcecode.File, L: sourcecode.Line): F[A] =
        filter(f)
    }
  }

  object Syntax extends Syntax

  import scalaz.MonadError, scalaz.syntax.monadError._

  type Location = (_root_.sourcecode.File,_root_.sourcecode.Line)

  case class LocationException(location: Location) extends Throwable {
    override def toString = location.toString
    override def getMessage = toString
  }

  def FilterForMonadError[F[_],E](implicit
    error: Location => E,
    merror: MonadError[F,E]) =
    new Filter[F]{
      def filter[A](fa: F[A])(f: A => Boolean)(implicit
        F: sourcecode.File, L: sourcecode.Line): F[A] =
        merror.bind(fa)(a =>
          if (f(a)) a.point[F] else merror.raiseError(error((F,L)))
        )
    }

  implicit def FilterForLocation[F[_]](
    implicit merror: MonadError[F,Location]) =
    FilterForMonadError(identity,merror)

  implicit def FilterForThrowable[F[_]](
    implicit merror: MonadError[F,Throwable]) =
    FilterForMonadError(LocationException(_), merror)
}
