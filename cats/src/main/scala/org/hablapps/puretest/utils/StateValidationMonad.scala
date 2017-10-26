package org.hablapps.puretest

import cats.Monad
import cats.data.{State, Validated}
import cats.kernel.Semigroup
import cats.implicits._

trait StateValidatedMonad{

  implicit def stateValidatedMonad[E: Semigroup, S]: Monad[λ[α => State[S, Validated[E, α]]]] =
    new Monad[λ[α=>State[S,Validated[E,α]]]]{

      def pure[A](a: A) =
        State.pure(a.valid[E])

      def flatMap[A, B](p: State[S,Validated[E, A]])(
          f: A => State[S,Validated[E, B]]) =
        State{ s =>
          val (s1, maybea) = p.run(s).value
          maybea.fold(
            error => (s1, error.invalid[B]),
            a => f(a).run(s1).value)
        }

      // TODO: Check this out!
      def tailRecM[A, B](a: A)(f: A => State[S, Validated[E, Either[A, B]]]) =
        State[S, Validated[E, B]] { s =>
          f(a).run(s).value match {
            case (s, v) =>
              (s, v.fold(
                e => e.invalid[B],
                {
                  case Left(a2) => tailRecM(a2)(f).runA(s).value
                  case Right(b) => b.valid[E]
                }
              ))
          }
        }

      override def ap[A,B](ff: State[S, Validated[E, A => B]])(fa: State[S, Validated[E, A]]) =
        State[S, Validated[E, B]] { s =>
          val (s1, maybea) = fa.run(s).value
          val (s2, maybefab) = ff.run(s1).value
          (s2, (maybefab |@| maybea).map{ _(_) })
        }
    }
}
