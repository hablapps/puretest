package org.hablapps.puretest

import scalaz.{Functor, Monad}
import scalaz.syntax.monadError._

class BooleanOps[P[_]](self: P[Boolean]) {
  def not(implicit F: Functor[P]): P[Boolean] =
    self.map(! _)

  def andThen(other: P[Boolean])(implicit M: Monad[P]): P[Boolean] =
    self.ifM(other,false.point[P])

  def orElse(other: P[Boolean])(implicit M: Monad[P]): P[Boolean] =
    self.ifM(true.point[P],other)

  def implies(other: P[Boolean])(implicit M: Monad[P]): P[Boolean] =
    self.ifM(other,true.point[P])
}


