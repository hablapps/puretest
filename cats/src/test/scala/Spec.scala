package org.hablapps.puretest
package test

import cats.{MonadState, MonadError}
import cats.implicits._
import Filter.Syntax._

trait Spec[P[_]]{

  val MS: MonadState[P, Int]
  implicit val ME: MonadError[P, Throwable]

  implicit val Fi: Filter[P]

  /* Working programs */

  def trueProgram: P[Boolean] = for {
    _ <- MS.set(1)
    1 <- MS.get
  } yield true

  def falseProgram: P[Boolean] =
    false.pure[P]

  def workingProgram: P[Unit] =
    ().pure[P]

  /* Boolean program that fails in pattern matching */

  def failingMatchBoolProgram: P[Boolean] = for {
    _ <- MS.set(1)
    2 <- MS.get
  } yield true

  def failingMatchProgram: P[Unit] = for {
    _ <- MS.set(1)
    2 <- MS.get
  } yield ()

  /* Failing and working programs with explicit raised errors */

  def raisedErrorBoolProgram: P[Boolean] =
    ME.raiseError(new RuntimeException("forced exception"))

  def raisedErrorProgram: P[Unit] =
    ME.raiseError(new RuntimeException("forced exception"))

  case class Error1(i: Int) extends Throwable

  def failingProgramWithHandledError: P[Unit] =
    for {
      Left(Error1(1)) <- ME.raiseError[Unit](Error1(1)).error
    } yield ()
}