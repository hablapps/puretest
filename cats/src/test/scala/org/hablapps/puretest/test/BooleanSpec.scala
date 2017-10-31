package org.hablapps.puretest
package test

trait BooleanSpec[P[_]] extends BooleanPrograms[P] with FunSpec[P] {

  Describe("Boolean programs"){
    Holds("if return true"){
      trueProgram
    }

    Holds("if not return false"){
      falseProgram not
    }

    Holds("AndThen"){
      (trueProgram andThen trueProgram) andThen
      (trueProgram andThen falseProgram).not andThen
      (falseProgram andThen trueProgram).not andThen
      (falseProgram andThen falseProgram).not
    }

    Holds("OrElse"){
      (trueProgram orElse trueProgram) andThen
      (trueProgram orElse falseProgram) andThen
      (falseProgram orElse trueProgram) andThen
      (falseProgram orElse falseProgram).not
    }

    Holds("Implies"){
      (trueProgram implies trueProgram) andThen
      (trueProgram implies falseProgram).not andThen
      (falseProgram implies trueProgram) andThen
      (falseProgram implies falseProgram)
    }
  }
}

object BooleanSpec{
  import cats.{MonadState, MonadError}

  class Scalatest[P[_]](
    val Tester: Tester[P, PuretestError[Throwable]])(implicit
    val MS: MonadState[P, Int],
    val ME: MonadError[P, Throwable],
    val RE: RaiseError[P, PuretestError[Throwable]])
  extends scalatestImpl.FunSpec[P, Throwable] with BooleanSpec[P]
}