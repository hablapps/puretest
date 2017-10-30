package org.hablapps
package puretest
package test


import scalaz._

import WorkingProgram.Error, WorkingSpecStateT.Program

class WorkingSpecStateT extends WorkingSpec.Scalatest[Program](
  WorkingProgram[Program],
  RaiseError[Program,PuretestError[Error]],
  StateTester[Program,Int,PuretestError[Error]].apply(0)
)

object WorkingSpecStateT{
  type Program[T] = StateT[PuretestError[Error] \/ ?, Int, T]
}