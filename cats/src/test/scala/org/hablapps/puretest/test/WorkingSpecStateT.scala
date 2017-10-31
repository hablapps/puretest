package org.hablapps
package puretest
package test

import cats.data.StateT
import cats.instances.either._

import WorkingProgram.Error, WorkingSpecStateT.Program
import PuretestError._

class WorkingSpecStateT extends WorkingSpec.Scalatest[Program](
  WorkingProgram[Program],
  RaiseError[Program, PuretestError[Error]],
  StateTester[Program, Int, PuretestError[Error]].apply(0)
)

object WorkingSpecStateT{
  type Program[T] = StateT[Either[PuretestError[Error], ?], Int, T]
}
