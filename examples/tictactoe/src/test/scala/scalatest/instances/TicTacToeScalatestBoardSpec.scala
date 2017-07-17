package org.hablapps.puretest.examples.tictactoe
package test

import org.scalatest._
import org.hablapps.puretest._
import cats.instances.either._

class BoardStateSpec extends FunSpec with Matchers
    with TicTacToeScalatestSpec[BoardState.Program]{

  val ticTacToe = BoardState.BoardTicTacToe
  val test = StateTester[BoardState.Program, BoardState, TicTacToe.Error]
}
