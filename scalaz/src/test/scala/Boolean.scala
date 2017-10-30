package org.hablapps.puretest
package test

import scalaz.{MonadState, MonadError}
import scalaz.syntax.monad._

trait BooleanPrograms[P[_]] extends FunSpec[P] {

  val MS: MonadState[P, Int]
  implicit val ME: MonadError[P, Throwable]
  implicit val RE: RaiseError[P, PuretestError[Throwable]]

  def trueProgram: P[Boolean] = for {
    _ <- MS.put(1)
    1 <- MS.get
  } yield true

  def falseProgram: P[Boolean] =
    false.point[P]

  def failingMatchBoolProgram: P[Boolean] = for {
    _ <- MS.put(1)
    2 <- MS.get
  } yield true

  def raisedErrorBoolProgram: P[Boolean] =
    ME.raiseError(new RuntimeException("forced exception"))
}
