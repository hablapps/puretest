package org.hablapps.puretest
package test

trait Spec[P[_]]{

  val MS: scalaz.MonadState[P,Int]
  implicit val ME: scalaz.MonadError[P,Throwable]

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

  /* Failing and working programs with explicit raised errors */
  
  import scalaz.MonadError

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