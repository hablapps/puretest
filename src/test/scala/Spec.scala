package org.hablapps.puretest
package test

trait Spec[P[_]]{

  implicit val MS: scalaz.MonadState[P,Int]
  implicit val Fi: Filter[P]

  import scalaz.syntax.monad._, Filter.Syntax._

  def test1: P[Boolean] = for {
    _ <- MS.put(1)
    2 <- MS.get
  } yield true

}