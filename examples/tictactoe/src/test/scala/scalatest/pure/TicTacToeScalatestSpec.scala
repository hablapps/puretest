package puretest
package examples.tictactoe
package test
package pure

import scalatestImpl.FunSpec
import cats.instances.either._

import BoardStateTest.Program

class BoardStateSpec extends FunSpec[Program, TicTacToe.Error]
    with test.TicTacToeSpec[Program] {

  val ticTacToe = BoardStateTest.Instance
  val Tester = StateTester[Program, BoardState, PuretestError[TicTacToe.Error]].apply(BoardState.empty)
  val RE = RaiseError[Program, PuretestError[TicTacToe.Error]]

}
