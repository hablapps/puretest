package org.hablapps.puretest.examples.tictactoe

import cats.MonadError
import cats.syntax.all._

trait TicTacToe[P[_]] {
  import TicTacToe._

  /* Evidences */

  implicit val ME: MonadError[P, Error]

  /* Transformers */

  def reset: P[Unit]

  def place(stone: Stone, position: Position): P[Unit]

  /* Observers */

  def win(stone: Stone): P[Boolean]

  def in(position: Position): P[Option[Stone]]

  def turn: P[Option[Stone]]

  /* Derived operations */

  def winner: P[Option[Stone]] =
    win(X).ifM(
      Option[Stone](X).pure[P],
      win(O).ifM(
        Option[Stone](O).pure[P],
        Option.empty.pure[P]))

  def currentTurnIs(stone: Stone): P[Boolean] =
    turn map { _ contains stone }

  def simulate(Xmoves: Position*)(Omoves: Position*): P[Stone] =
    reset >>
    simulate(Xmoves.toList, X, Omoves.toList)

  def simulate(moves: List[Position],
      stone: Stone,
      opponent: List[Position]): P[Stone] =
    moves match {
      case Nil =>
        ME.raiseError(NotEnoughMoves)
      case m :: ms =>
        place(stone, m) >>
        win(stone).ifM(
          stone.pure[P],
          simulate(opponent, stone.opponent, ms))
    }
}

object TicTacToe {

  // Positions of the board

  type Position = (Int, Int)

  // The two types of stones for both players, which are
  // simply represented by boolean values.

  sealed abstract class Stone {
    val opponent: Stone
  }
  case object O extends Stone { val opponent = X }
  case object X extends Stone { val opponent = O }

  // Errors

  // scalastyle:off
  def OccupiedPosition(position: Position): Error = Error.OccupiedPosition(position)
  def NotInTheBoard(position: Position): Error = Error.NotInTheBoard(position)
  def WrongTurn(turn: Stone): Error = Error.WrongTurn(turn)
  val NotEnoughMoves: Error = Error.NotEnoughMoves
  val GameOver: Error = Error.GameOver
  // scalastyle:on

  sealed abstract class Error
  object Error {
    case class OccupiedPosition(position: Position) extends Error
    case class NotInTheBoard(position: Position) extends Error
    case class WrongTurn(turn: Stone) extends Error
    case object NotEnoughMoves extends Error
    case object GameOver extends Error
  }

}
