package org.hablapps

package object puretest
  extends StateTMonadError
  with StateTEqual
  with StateTArbitrary
  with StateValidationMonad
  with TestingOps
