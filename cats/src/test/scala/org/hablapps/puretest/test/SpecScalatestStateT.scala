package org.hablapps.puretest
package test

import cats.{MonadError, MonadState}
import cats.data.StateT
import cats.instances.either._

import SpecScalatestStateT.Program

class SpecScalatestStateT extends scalatestImpl.ScalatestFunSpec[Program, Throwable] with Spec[Program] {

  val MS = MonadState[Program, Int]
  implicit val MPE = MonadError[Program, PuretestError[Throwable]]
  val ME = MonadError[Program, Throwable]
  val RE = RaiseError[Program, PuretestError[Throwable]]

  val Tester = StateTester[Program, Int, PuretestError[Throwable]].apply(0)
}

object SpecScalatestStateT {
  type Program[T] = StateT[Either[PuretestError[Throwable], ?], Int, T]
}