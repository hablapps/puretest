package org.hablapps.puretest
package test

trait Spec[P[_]]{

  implicit val MS: scalaz.MonadState[P,Int]
  implicit val Fi: Filter[P]

  import scalaz.syntax.monad._, Filter.Syntax._

  /* Working programs */

  def trueProgram: P[Boolean] = for {
    _ <- MS.put(1)
    1 <- MS.get
  } yield true

  def falseProgram: P[Boolean] = 
    false.point[P]

  def workingProgram: P[Unit] = 
    ().point[P]

  /* Boolean program that fails in pattern matching */

  def failingMatchBoolProgram: P[Boolean] = for {
    _ <- MS.put(1)
    2 <- MS.get
  } yield true

  def failingMatchProgram: P[Unit] = for {
    _ <- MS.put(1)
    2 <- MS.get
  } yield ()

  /* Failing programs with explicit raised errors */
  
  import scalaz.MonadError

  def raisedErrorBoolProgram(
    implicit ME: MonadError[P,Throwable]): P[Boolean] = 
    ME.raiseError(new RuntimeException("forced exception"))

  def raisedErrorProgram(
    implicit ME: MonadError[P,Throwable]): P[Unit] = 
    ME.raiseError(new RuntimeException("forced exception"))
}