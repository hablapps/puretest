package org.hablapps
package puretest
package test

import org.scalatest._
import puretest.{Filter => TestFilter}

trait SpecScalatest[P[_]] extends Spec[P]{ self : FunSpec with Matchers =>

  implicit val Te: StateTester[P,Int,Throwable]

  // Tests

  import ProgramStateMatchers.Syntax._

  describe("beSatisfied for Boolean program"){

    it("should work for working programs returning true"){
      trueProgram should from(0).beSatisfied
    }

    it("should work for working programs returning false"){
      falseProgram should not(from(0).beSatisfied)
    }

    it("should work for failing programs at pattern matching"){
      failingMatchBoolProgram should not(from(0).beSatisfied)
    }

    it("should work for failing programs with explicit raised errors"){
      raisedErrorBoolProgram should not(from(0).beSatisfied)
    }
  }

  describe("runWithoutErrors"){

    it("should work for working programs"){
      trueProgram should from(0).runWithoutErrors
      falseProgram should from(0).runWithoutErrors
      workingProgram should from(0).runWithoutErrors
    }

    it("should work for failing programs at pattern matching"){
      failingMatchBoolProgram should not(from(0).runWithoutErrors)
      failingMatchProgram should not(from(0).runWithoutErrors)
    }

    it("should work for failing programs with explicit raised errors"){
      raisedErrorBoolProgram should not(from(0).runWithoutErrors)
      raisedErrorProgram should not(from(0).runWithoutErrors)
    }

    it("should work for failing programs with handled errors"){
      failingProgramWithHandledError should from(0).runWithoutErrors
    }
  }
}
