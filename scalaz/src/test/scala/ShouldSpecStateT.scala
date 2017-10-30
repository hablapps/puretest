package org.hablapps
package puretest
package test


import scalaz._

import WorkingProgram.Error, ShouldSpecStateT.Program

class ShouldSpecStateT extends ShouldSpec.Scalatest[Program](
  WorkingProgram[Program](implicitly,
    PuretestError.toMonadError(MonadError[Program,PuretestError[Error]])),
  implicitly, //HandleError[Program,PuretestError[Error]],
  implicitly, // RaiseError[Program,PuretestError[Error]],
  implicitly, // RaiseError[Program,PuretestError[PuretestError[Error]]],
  StateTester[Program,Int,PuretestError[PuretestError[Error]]].apply(0)
)

object ShouldSpecStateT{
  type Program[T] = StateT[PuretestError[PuretestError[Error]] \/ ?, Int, T]
}