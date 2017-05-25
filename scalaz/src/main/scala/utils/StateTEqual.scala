package org.hablapps.puretest

import org.scalacheck._

import scalaz._, Scalaz._

trait StateTEqual extends StateTEqual0 {

  implicit def stateEqual[S, A](implicit
      as: Arbitrary[S],
      eq1: Equal[S],
      eq2: Equal[A]): Equal[State[S, A]] =
    stateTEqual[Id, S, A]
}

trait StateTEqual0 {

  implicit def stateTEqual[F[_], S, A](implicit
      m: Monad[F],
      as: Arbitrary[S],
      eq1: Equal[S],
      eq2: Equal[A],
      eq3: Equal[F[(S, A)]]): Equal[StateT[F, S, A]] =
    Equal.equal[StateT[F, S, A]] { (st1, st2) =>
      Gen.listOfN(50, as.arbitrary).sample
        .getOrElse(sys.error("could not generate arbitrary list of init states"))
        .forall(s => st1(s) === st2(s))
    }
}
