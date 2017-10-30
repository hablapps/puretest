package org.hablapps.puretest
package test

import scalaz.syntax.monad._
import WorkingProgram.Error

trait ShouldSpec[P[_]] extends FunSpec[P] {
  val S: WorkingProgram[P]
  import S._

  implicit val HE1: HandleError[P, PuretestError[Error]]
  implicit val RE1: RaiseError[P, PuretestError[Error]]
  implicit val RE2: RaiseError[P, PuretestError[PuretestError[Error]]]

  Describe("ShouldFail should fail"){
    It("with working programs"){
      workingProgram.shouldFail[Error].
        shouldFail[PuretestError[Error]](NotFailed[Error,Int](1)) >>
      (for {
        2 <- workingProgramWithHandledError
      } yield ()).shouldFail[Error].shouldFail[PuretestError[Error]]
    }

    It("if expected error doesnt' equal actual error"){
      failingProgram.shouldFail[Error](Error(1))
        .shouldFail[PuretestError[Error]](OtherError[Error](Error(0),Error(1)))
    }

    It("if actual error doesn't match pattern"){
      failingProgram.shouldMatchFailure[Error]{ _ == Error(1) }
        .shouldFail[PuretestError[Error]](NotMatchedFailure[Error](Error(0)))
    }
  }

  Describe("ShouldFail should succeed"){
    It("with failing programs"){
      failingProgram.shouldFail[Error].shouldSucceed
    }

    It("if expected error equals actual error"){
      failingProgram.shouldFail[Error](Error(0)).shouldSucceed
    }

    It("if actual error matches pattern"){
      failingProgram.shouldMatchFailure[Error]{ _ == Error(0) }.shouldSucceed
    }
  }

  Describe("ShouldSucceed should fail"){
    It("with failing programs"){
      failingProgram.shouldSucceed[Error]
        .shouldFail[PuretestError[Error]](NotSucceeded[Error](Error(0)))
    }

    It("if expected value doesn't equal actual value"){
      workingProgram.shouldBe[Error](2)
        .shouldFail[PuretestError[Error]](NotEqualTo[Error,Int](1,2))
    }

    It("if it doesn't match actual value"){
      (MS.put(1) >> MS.get).shouldMatch[Error]{ _ == 2 }
        .shouldFail[PuretestError[Error]]
    }
  }

  Describe("ShouldSucceed should succeed"){
    It("with working programs"){
      workingProgram.shouldSucceed[Error]
        .shouldBe[PuretestError[Error]](1)
    }

    It("if expected value equals actual value"){
      workingProgram.shouldBe[Error](1)
        .shouldBe[PuretestError[Error]](1)
    }

    It("if it matches actual value"){
      (MS.put(1) >> MS.get).shouldMatch[Error]{ _ == 1 }
        .shouldBe[PuretestError[Error]](1)
    }
  }
}

object ShouldSpec{
  class Scalatest[P[_]](
    val S: WorkingProgram[P],
    val HE1: HandleError[P, PuretestError[Error]],
    val RE1: RaiseError[P, PuretestError[Error]],
    val RE2: RaiseError[P, PuretestError[PuretestError[Error]]],
    val Tester: Tester[P,PuretestError[PuretestError[Error]]])
  extends scalatestImpl.ScalatestFunSpec[P,PuretestError[Error]]
  with ShouldSpec[P]
}



