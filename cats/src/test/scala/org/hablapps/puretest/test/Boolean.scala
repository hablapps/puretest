package org.hablapps.puretest
package test

import cats.{MonadState, MonadError}
import cats.syntax.all._

trait BooleanPrograms[P[_]] {

  val MS: MonadState[P, Int]
  implicit val ME: MonadError[P, Throwable]
  implicit val RE: RaiseError[P, PuretestError[Throwable]]

  def trueProgram: P[Boolean] = for {
    _ <- MS.set(1)
    1 <- MS.get
  } yield true

  def falseProgram: P[Boolean] =
    false.pure[P]

  def failingMatchBoolProgram: P[Boolean] = for {
    _ <- MS.set(1)
    2 <- MS.get
  } yield true

  def raisedErrorBoolProgram: P[Boolean] =
    ME.raiseError(new RuntimeException("forced exception"))
}
