package org.hablapps.puretest

trait StateValidationMonad{
  import scalaz.{Semigroup, Monad}, scalaz.{State, Validation}
  import scalaz.syntax.validation._

  implicit def StateValidationMonad[E: Semigroup,S]: Monad[λ[α=>State[S,Validation[E,α]]]] =
    new Monad[λ[α=>State[S,Validation[E,α]]]]{

      def point[A](a: => A) =
        State.state(a.success[E])

      def bind[A,B](p: State[S,Validation[E,A]])(
        f: A => State[S,Validation[E,B]]) =
        State{ s =>
          val (s1,maybea) = p(s)
          maybea.fold(
            error => (s1, error.failure[B]),
            a => f(a)(s1))
        }

      import scalaz.syntax.apply._

      override def ap[A,B](p: => State[S,Validation[E,A]])(
        f: => State[S, Validation[E,A=>B]]) =
        State{ s =>
          val (s1, maybea) = p(s)
          val (s2, maybefab) = f(s1)
          (s2, (maybefab |@| maybea){ _(_) })
        }
    }
}
