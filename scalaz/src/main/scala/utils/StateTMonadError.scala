package org.hablapps.puretest

import scalaz.{MonadError, StateT, IndexedStateT}

trait StateTMonadError{

  implicit def stateTMonadError[S, F[_], E](implicit F: MonadError[F, E]) =
    new MonadError[StateT[F, S, ?], E]{

      import scalaz.Need

      def point[A](a: => A): StateT[F, S, A] = {
        val aa = Need(a)
        StateT(s => F.point(s, aa.value))
      }

      def bind[A, B](fa: StateT[F, S, A])(f: A => StateT[F, S, B]): StateT[F, S, B] =
        fa.flatMap(f)

      def raiseError[A](e: E): StateT[F,S,A] =
        IndexedStateT(_ => F.raiseError(e))

      def handleError[A](fa: StateT[F,S,A])(
        f: E => StateT[F,S,A]): StateT[F,S,A] =
          fa.mapsf(sf => (s: S) =>
            F.handleError(sf(s)){ e =>
              val fe: F[S=>F[(S,A)]] = f(e).getF(F)
              F.bind(fe)(ff => ff(s))
            }
          )
    }
}
