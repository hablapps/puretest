package org.hablapps.puretest

import cats.~>

/**
 * Testers
 */
trait Tester[P[_], E] extends (P ~> Either[E, ?])

object Tester{
  def apply[P[_], E](implicit T: Tester[P, E]) = T

  /* Testing Option programs */

  implicit def optionTester: Tester[Option, Unit] =
    new Tester[Option, Unit]{
      def apply[X](e: Option[X]) = e.fold(Left(()): Either[Unit,X])(Right.apply)
    }

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

  /* Validated programs */

  import cats.data.Validated

  implicit def validatedTester[E]: Tester[Validated[E, ?], E] =
    new Tester[Validated[E, ?], E] {
      def apply[A](fa: Validated[E, A]): Either[E, A] = fa.toEither
    }

  /* Composing testers */

  implicit def composedTester[F[_], G[_], E](implicit
      F: Tester[F, E],
      G: Tester[G, E]): Tester[λ[α => F[G[α]]], E] =
    new Tester[λ[α => F[G[α]]], E] {
      def apply[A](fa: F[G[A]]): Either[E, A] =
        F(fa).right.flatMap(G.apply)
    }

  def composedTesterOuter[F[_], G[_], EF, EG](f: EG => EF)(implicit
      F: Tester[F, EF],
      G: Tester[G, EG]): Tester[λ[α => F[G[α]]], EF] =
    new Tester[λ[α => F[G[α]]], EF] {
      def apply[A](fa: F[G[A]]): Either[EF, A] =
        F(fa).right flatMap { ga =>
          G(ga).fold(
            f andThen Left.apply,
            Right.apply)
        }
    }

  def composedTesterInner[F[_], G[_], EF, EG](f: EF => EG)(implicit
      F: Tester[F, EF],
      G: Tester[G, EG]): Tester[λ[α => F[G[α]]], EG] =
    new Tester[λ[α => F[G[α]]], EG] {
      def apply[A](fa: F[G[A]]): Either[EG, A] =
        F(fa).fold(
          f andThen Left.apply,
          G.apply)
    }

}
