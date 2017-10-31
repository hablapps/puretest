package org.hablapps.puretest
package test

import cats.MonadError
import cats.data.StateT
import cats.instances.either._

import WorkingProgram.Error, ShouldSpecStateT.Program
import PuretestError._

class ShouldSpecStateT extends ShouldSpec.Scalatest[Program](
  WorkingProgram[Program](implicitly,
    PuretestError.toMonadError(MonadError[Program, PuretestError[Error]])),
  HandleError[Program, PuretestError[Error]],
  RaiseError[Program, PuretestError[Error]],
  RaiseError[Program, PuretestError[PuretestError[Error]]],
  StateTester[Program, Int, PuretestError[PuretestError[Error]]].apply(0)
)

object ShouldSpecStateT{
  type Program[T] = StateT[Either[PuretestError[PuretestError[Error]], ?], Int, T]
}
