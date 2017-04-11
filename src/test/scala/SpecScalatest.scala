package org.hablapps
package puretest
package test

import org.scalatest._
import puretest.{Filter => TestFilter}, scalatestImpl._

trait SpecScalatest[P[_]] extends Spec[P]{ self : FunSpec with Matchers =>

  implicit val Te: StateTester[P,Int,Throwable]

  // Tests

  import ProgramStateMatchers.Syntax._

  describe("MonadState tests"){
    it("should work"){
      test1 should beSatisfied(from = 0)
    }
  }
}
