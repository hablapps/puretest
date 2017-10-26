package org.hablapps

package object puretest
  extends StateValidatedMonad
  with MonadErrorUtils
  with Filter.Syntax {

  type Location = (sourcecode.File, sourcecode.Line)

  implicit def loc(implicit f: sourcecode.File, l: sourcecode.Line) = (f, l)

  /* matchers and ops */

  import cats.Monad

  implicit def toPureMatchers[P[_], A](self: P[A])(implicit
    M: Monad[P],
    loc: Location) = new PureMatchers(self)

  implicit def toBooleanOps[P[_]](p: P[Boolean]) =
    new BooleanOps(p)

}
