package org.hablapps
package puretest
package test

import cats.{MonadError, MonadState}
import cats.data.StateT
import cats.implicits._
import org.scalatest._
import puretest.{Filter => TestFilter}

import SpecScalatestStateT.Program

class SpecScalatestStateT extends FunSpec with Matchers with SpecScalatest[Program]{

  val MS = MonadState[Program, Int]
  val ME = MonadError[Program, Throwable]
  val Fi = TestFilter[Program]
  val Te = StateTester[Program, Int, Throwable]
}

object SpecScalatestStateT {
  type Program[T] = StateT[Either[Throwable, ?], Int, T]
}