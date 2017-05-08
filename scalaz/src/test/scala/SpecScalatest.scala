package org.hablapps
package puretest
package test

import org.scalatest._
import puretest.{Filter => TestFilter}, scalatestImpl._

trait SpecScalatest[P[_]] extends Spec[P]{ self : FunSpec with Matchers =>

  implicit val Te: StateTester[P,Int,Throwable]
  
  // Tests

  import ProgramStateMatchers.Syntax._

  describe("beSatisfied for Boolean program"){
    
    it("should work for working programs returning true"){
      trueProgram should beSatisfied(from = 0)
    }

    it("should work for working programs returning false"){
      falseProgram should not(beSatisfied(from = 0))
    }

    it("should work for failing programs at pattern matching"){
      failingMatchBoolProgram should not(beSatisfied(from = 0))
    }

    it("should work for failing programs with explicit raised errors"){
      raisedErrorBoolProgram should not(beSatisfied(from = 0))
    }
  }

  describe("runWithoutErrors"){

    it("should work for working programs"){
      trueProgram should runWithoutErrors(from = 0)
      falseProgram should runWithoutErrors(from = 0)
      workingProgram should runWithoutErrors(from = 0)
    }

    it("should work for failing programs at pattern matching"){
      failingMatchBoolProgram should not(runWithoutErrors(from = 0))
      failingMatchProgram should not(runWithoutErrors(from = 0))
    }

    it("should work for failing programs with explicit raised errors"){
      raisedErrorBoolProgram should not(runWithoutErrors(from = 0))
      raisedErrorProgram should not(runWithoutErrors(from = 0))
    }

    it("should work for failing programs with handled errors"){
      failingProgramWithHandledError should runWithoutErrors(from = 0)
    }
  }
}
