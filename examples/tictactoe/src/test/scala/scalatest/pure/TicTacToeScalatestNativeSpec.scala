package org.hablapps.puretest.examples.tictactoe
package test
package pure

import org.scalatest._
import cats.instances.either._
import cats.syntax.all._

import org.hablapps.puretest.ApplicationError

class TicTacToeSpecNative extends FunSpec with Matchers {
  import TicTacToe._
  import BoardState.Instance._

  describe("Reset Spec") {
    it("First turn is X") {
      (reset >>
      currentTurnIs(X)).runA(BoardState.empty) shouldBe Right(true)
    }
  }

  describe("Place Spec") {
    it("should not be possible to place more than one stone at the same place") {
      (reset >>
      place(X, (1, 1)) >>
      place(O, (1, 1))).runA(BoardState.empty) shouldBe Left(ApplicationError(OccupiedPosition((1, 1))))
    }

    it("Placing outside of the board is error") {
      (reset >>
      place(X, (5, 5))).runA(BoardState.empty) shouldBe Left(ApplicationError(NotInTheBoard((5, 5))))
    }

    it("Placing in the wrong turn") {
      (reset >>
      place(O, (1, 1))).runA(BoardState.empty) shouldBe Left(ApplicationError(WrongTurn(O)))
    }

    it("Turn must change") {
      ((reset >>
        place(X, (1, 1)) >>
        currentTurnIs(O)) &&
      (place(O, (1, 2)) >>
        currentTurnIs(X))).runA(BoardState.empty) shouldBe Right(true)
    }

    // it("Position must be occupied") {
    //   (for {
    //     _ <- reset
    //     _ <- place(X, (1, 1))
    //     Some(X) <- in((1, 1))
    //   } yield ()).runA(BoardState.empty) shouldBe Right(())
    // }
  }

  describe("Win laws") {

    val winnerBoard: BoardState.Program[Unit] =
      reset >>
      place(X, (0, 0)) >>
      place(O, (1, 0)) >>
      place(X, (0, 1)) >>
      place(O, (2, 0)) >>
      place(X, (0, 2))

    it("Win at rows") {
      (winnerBoard >>
      win(X)).runA(BoardState.empty) shouldBe Right(true)
    }

    it("No simultaneous winners") {
      (winnerBoard >>
      win(O)).runA(BoardState.empty) shouldBe Right(false)
    }

    it("No winner if not over") {
      (reset >>
      winner).runA(BoardState.empty) shouldBe Right(None)
    }
  }

  describe("Simulation behaviour") {
    it("Unfinished match") {
      (reset >>
      simulate((0, 0), (0, 1))((1, 0), (1, 1))).runA(BoardState.empty) shouldBe Left(ApplicationError(NotEnoughMoves))
    }

    it("Finished match") {
      (reset >>
      simulate((0, 0), (0, 1), (0, 2))((1, 0), (1, 1), (1, 2)) >>
      win(X)).runA(BoardState.empty) shouldBe Right(true)
    }
  }
}
