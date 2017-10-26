package org.hablapps.puretest.examples

import cats.{Apply, Monad}
import cats.syntax.all._

package object tictactoe {

  implicit class PredicateOps[P[_]](self: P[Boolean]) {

    def ifThen(p: P[Unit])(implicit M: Monad[P]): P[Unit] =
      self.ifM(p, ().pure[P])

    def ifThenOpt[A](p: P[A])(implicit M: Monad[P]): P[Option[A]] =
      self.ifM(p.map(Option.apply), Option.empty.pure[P])

    def &&(other: P[Boolean])(implicit M: Apply[P]): P[Boolean] = // scalastyle:ignore
      (self |@| other).map(_ && _)
  }

}
