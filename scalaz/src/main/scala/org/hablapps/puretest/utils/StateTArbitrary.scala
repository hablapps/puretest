package org.hablapps.puretest

import org.scalacheck._

import scalaz._, Scalaz._

trait StateTArbitrary extends StateTArbitrary0 {

  implicit def stateArbitrary[S, A](implicit
      as: Arbitrary[S],
      aa: Arbitrary[A],
      cs: Cogen[S]): Arbitrary[State[S, A]] =
    stateTArbitrary[Id, S, A]
}

trait StateTArbitrary0 {

  implicit def stateTArbitrary[F[_], S, A](implicit
      m: Monad[F],
      as: Arbitrary[S],
      afsa: Arbitrary[F[(S, A)]],
      cs: Cogen[S]): Arbitrary[StateT[F, S, A]] =
    Arbitrary(Gen.resultOf[S => F[(S, A)], StateT[F, S, A]](StateT.apply))
}
