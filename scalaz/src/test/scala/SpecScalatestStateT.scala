package org.hablapps
package puretest
package test

import org.scalatest._
import puretest.{Filter => TestFilter}, scalatestImpl._

import SpecScalatestStateT.Program

class SpecScalatestStateT extends FunSpec with Matchers with SpecScalatest[Program]{

  val MS = scalaz.MonadState[Program,Int]
  val ME = scalaz.MonadError[Program,Throwable]
  val Fi = TestFilter[Program]
  val Te = StateTester[Program,Int,Throwable]
}

object SpecScalatestStateT{
  import scalaz.{\/, StateT}

  type Program[T]=StateT[Throwable \/ ?, Int, T]
}