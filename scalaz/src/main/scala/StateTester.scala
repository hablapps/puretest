package org.hablapps.puretest

/** 
 * Stateful testers
 */
trait StateTester[P[_], S, E] {
  def apply(state: S): Tester[P, E]
}

object StateTester {
  def apply[P[_], S, E](implicit T: StateTester[P, S, E]) = T

  import scalaz.{Monad, StateT}

  implicit def StateTStateTester[E, F[_]: Tester[?[_], E]: Monad, S] =
    new StateTester[StateT[F, S, ?], S, E] {
      def apply(state: S) = new Tester[StateT[F, S, ?], E]{
        def apply[T](s: StateT[F, S, T]): Either[E, T] = 
          Tester[F, E].apply(s.eval(state))
      }
    }

  import scalaz.ReaderT

  implicit def ReaderTStateTester[E, F[_]: Tester[?[_], E], S] =
    new StateTester[ReaderT[F, S, ?], S, E] {
      def apply(state: S) = new Tester[ReaderT[F, S, ?], E]{
        def apply[T](s: ReaderT[F,S,T]): Either[E, T] = 
          Tester[F, E].apply(s.run(state))
      }
    }

}
