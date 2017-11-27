package puretest
package test

import cats.data.StateT
import cats.instances.either._
import cats.mtl.instances.state._

import BooleanSpecStateT.Program
import PuretestError._

class BooleanSpecStateT extends BooleanSpec.Scalatest[Program](
  StateTester[Program, Int, PuretestError[Throwable]].apply(0))

object BooleanSpecStateT {
  type Program[T] = StateT[Either[PuretestError[Throwable], ?], Int, T]
}
