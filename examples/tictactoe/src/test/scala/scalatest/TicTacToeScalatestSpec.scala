package org.hablapps.puretest.examples.tictactoe
package test

import org.scalatest._
import org.hablapps.puretest._, ProgramStateMatchers.Syntax._

trait TicTacToeScalatestSpec[P[_]]
  extends TicTacToeSpec[P]{ self: FunSpec with Matchers =>

  implicit val test: StateTester[P, BoardState, TicTacToe.Error]

  val initial = BoardState.empty

  describe("Reset Spec"){

    it("First turn X"){
      firstTurnIsX should from(initial).beSatisfied
    }
  }

  describe("Place Spec"){
    it("should not be possible to place more than one stone at the same place"){
      occupiedPositionError should from(initial).beSatisfied
    }

    it("Placing outside of the board is error"){
      canOnlyPlaceInBoard should from(initial).beSatisfied
    }

    it("Placing in the wrong turn"){
      placingInTheWrongTurn should from(initial).beSatisfied
    }

    it("Turn must change"){
      turnMustChange should from(initial).beSatisfied
    }

    it("Position must be occupied"){
      positionMustBeOccupied should from(initial).beSatisfied
    }
  }

  describe("Win laws"){
    it("Win at rows"){
      winAtRows should from(initial).beSatisfied
    }

    it("No simultaneous winners"){
      noSimultaneousWinners should from(initial).beSatisfied
    }

    it("No winner if not over"){
      noWinnersIfNotOver should from(initial).beSatisfied
    }
  }

  describe("Simulation behaviour"){
    it("Unfinished match"){
      unfinishedMatch should from(initial).beSatisfied
    }

    it("Finished match"){
      finishedMatch should from(initial).beSatisfied
    }
  }

}
