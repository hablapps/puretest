package org.hablapps.puretest

import cats.Monad
import cats.data.{ReaderT, StateT}

/**
 * Stateful testers
 */
trait StateTester[P[_], S, E] {
  def apply(state: S): Tester[P,E]
}

object StateTester {
  def apply[P[_], S, E](implicit T: StateTester[P, S, E]) = T

  implicit def stateTStateTester[E, F[_]: Tester[?[_], E]: Monad, S] =
    new StateTester[StateT[F, S, ?], S, E] {
      def apply(state: S) = new Tester[StateT[F, S, ?], E]{
        def apply[T](s: StateT[F, S, T]): Either[E, T] =
          Tester[F, E].apply(s.runA(state))
      }
    }

  implicit def readerTStateTester[E, F[_]: Tester[?[_], E], S] =
    new StateTester[ReaderT[F, S, ?], S, E] {
      def apply(state: S) = new Tester[ReaderT[F, S, ?],E]{
        def apply[T](s: ReaderT[F,S,T]): Either[E, T] =
          Tester[F,E].apply(s.run(state))
      }
    }

  /* Wrap in any other Tester */
  implicit def testerStateTester[F[_], G[_], S, E](implicit
      ev1: Tester[F, E],
      ev2: StateTester[G, S, E]): StateTester[λ[α => F[G[α]]], S, E] =
    new StateTester[λ[α => F[G[α]]], S, E] {
      def apply(state: S): Tester[λ[α => F[G[α]]], E] =
        new Tester[λ[α => F[G[α]]], E] {
          def apply[A](fa: F[G[A]]): Either[E, A] =
            ev1(fa).right.flatMap { ga =>
              ev2(state)(ga)
            }
        }
    }

  def testerStateTesterInner[F[_], G[_], S, EF, EG](f: EF => EG)(implicit
      ev1: Tester[F, EF],
      ev2: StateTester[G, S, EG]): StateTester[λ[α => F[G[α]]], S, EG] =
    new StateTester[λ[α => F[G[α]]], S, EG] {
      def apply(state: S): Tester[λ[α => F[G[α]]], EG] =
        new Tester[λ[α => F[G[α]]], EG] {
          def apply[A](fa: F[G[A]]): Either[EG, A] =
            ev1(fa).fold(
              f andThen Left.apply,
              ev2(state).apply)
        }
    }

  def testerStateTesterOuter[F[_], G[_], S, EF, EG](f: EG => EF)(implicit
      ev1: Tester[F, EF],
      ev2: StateTester[G, S, EG]): StateTester[λ[α => F[G[α]]], S, EF] =
    new StateTester[λ[α => F[G[α]]], S, EF] {
      def apply(state: S): Tester[λ[α => F[G[α]]], EF] =
        new Tester[λ[α => F[G[α]]], EF] {
          def apply[A](fa: F[G[A]]): Either[EF, A] =
            ev1(fa).right flatMap { ga =>
              ev2(state)(ga).fold(
                f andThen Left.apply,
                Right.apply)
            }
        }
    }

}
