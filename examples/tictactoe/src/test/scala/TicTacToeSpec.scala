package org.hablapps.puretest.examples.tictactoe
package test

import cats.implicits._
import org.hablapps.puretest._

trait TicTacToeSpec[P[_]] {

  /* Evidence */

  val ticTacToe: TicTacToe[P]

  /* Predicates */
  import ticTacToe._, TicTacToe._

  // RESET

  def firstTurnIsX: P[Boolean] =
    reset >>
    currentTurnIs(X)

  // PLACE

  def occupiedPositionError: P[Boolean] =
    reset >>
    place(X,(1,1)) >>
    place(O,(1,1)).isError[Error](OccupiedPosition((1,1)))

  def canOnlyPlaceInBoard: P[Boolean] =
    reset >>
    place(X,(5,5)).isError[Error](NotInTheBoard((5,5)))

  def placingInTheWrongTurn: P[Boolean] =
    reset >>
    place(O,(1,1)).isError[Error](WrongTurn(O))

  def turnMustChange: P[Boolean] =
    (reset >>
      place(X,(1,1)) >>
      currentTurnIs(O)) &&
    (place(O,(1,2)) >>
      currentTurnIs(X))

  def positionMustBeOccupied: P[Boolean] =
    reset >>
    place(X,(1,1)) >>
    in((1,1)) map (_.fold(false)(_ == X))

  // WINNER

  def winnerBoard: P[Unit] =
    reset >>
    place(X,(0,0)) >>
    place(O,(1,0)) >>
    place(X,(0,1)) >>
    place(O,(2,0)) >>
    place(X,(0,2))

  def winAtRows: P[Boolean] =
    winnerBoard >>
    win(X)

  def noSimultaneousWinners: P[Boolean] =
    winnerBoard >>
    win(O) map (! _)

  def noWinnersIfNotOver: P[Boolean] =
    reset >>
    winner() map (! _.isDefined)

  // SIMULATION

  def finishedMatch: P[Boolean] =
    reset >>
    simulate((0,0),(0,1),(0,2))((1,0),(1,1),(1,2)) >>
    win(X)

  def unfinishedMatch: P[Boolean] =
    reset >>
    simulate((0,0),(0,1))((1,0),(1,1)).isError[Error](NotEnoughMoves())
}
