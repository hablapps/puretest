package org.hablapps.puretest

import cats.{Functor, Monad}
import cats.syntax.all._

class BooleanOps[P[_]](self: P[Boolean]) {
  def not(implicit F: Functor[P]): P[Boolean] =
    self.map(! _)

  def andThen(other: P[Boolean])(implicit M: Monad[P]): P[Boolean] =
    self.ifM(other,false.pure[P])

  def orElse(other: P[Boolean])(implicit M: Monad[P]): P[Boolean] =
    self.ifM(true.pure[P],other)

  def implies(other: P[Boolean])(implicit M: Monad[P]): P[Boolean] =
    self.ifM(other,true.pure[P])
}
