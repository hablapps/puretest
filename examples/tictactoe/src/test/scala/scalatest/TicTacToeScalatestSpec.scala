package org.hablapps.puretest
package examples.tictactoe
package test

import scalatestImpl.ScalatestFunSpec
import cats.instances.either._

import BoardState.Program

class BoardStateSpec extends ScalatestFunSpec[Program, TicTacToe.Error]
    with TicTacToeSpec[Program] {

  val ticTacToe = BoardState.BoardTicTacToe
  val Tester = StateTester[Program, BoardState, PuretestError[TicTacToe.Error]].apply(BoardState.empty)
  val RE = RaiseError[Program, PuretestError[TicTacToe.Error]]

}
