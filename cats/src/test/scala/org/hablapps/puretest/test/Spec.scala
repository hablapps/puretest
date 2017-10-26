package org.hablapps.puretest
package test

import cats.{MonadError, MonadState}
import cats.syntax.all._

trait Spec[P[_]] extends FunSpec[P] {

  val MS: MonadState[P, Int]
  implicit val ME: MonadError[P, Throwable]
  implicit val RE: RaiseError[P, PuretestError[Throwable]]

  /* Working programs */

  Describe("beSatisfied for Boolean program"){

    It("should work for working programs returning true") {
      trueProgram
    }

    It("should work for failing programs at pattern matching") {
      falseProgram shouldBe false
    }

    It("should work for failing programs with explicit raised errors") {
      raisedErrorBoolProgram.shouldFail
    }
  }

  Describe("runWithoutErrors"){

    It("should work for working programs"){
      trueProgram.shouldSucceed
      falseProgram shouldBe false
      workingProgram shouldMatch { case () => true }
    }

    It("should fail for failing programs at pattern matching") {
      failingMatchBoolProgram.shouldFail
      failingMatchProgram.shouldFail
    }

    It("should work for failing programs with explicit raised errors"){
      raisedErrorBoolProgram.shouldFail
      raisedErrorProgram.shouldFail
    }

    It("should work for failing programs with handled errors"){
      failingProgramWithHandledError.shouldSucceed
    }
  }

  def trueProgram: P[Unit] = for {
    _ <- MS.set(1)
    1 <- MS.get
  } yield ()

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
      Left(Error1(1)) <- ME.raiseError[Unit](Error1(1)).attempt
    } yield ()
}