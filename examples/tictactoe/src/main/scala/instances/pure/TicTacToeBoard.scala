package org.hablapps.puretest
package examples.tictactoe

import TicTacToe._

import cats.MonadError
import cats.data.StateT
import cats.instances.either._
import cats.syntax.all._

case class BoardState(
  board: Vector[Vector[Option[Stone]]],
  turn: Stone)

object BoardState {

  /* Auxiliary types */

  type Program[T] = StateT[Either[Error, ?], BoardState, T]

  /* Auxiliary values */

  val empty = BoardState(
    board = Vector.fill(3)(Vector.fill(3)(None)),
    turn = X)

  /* Instance */

  object Instance extends TicTacToe[Program] {

    /* Evidences */

    val ME = MonadError[Program, Error]

    import StateT._

    /* Transformers */

    def reset: Program[Unit] =
      set(empty)

    def place(stone: Stone, position: Position): Program[Unit] =
      checkOutsideBoard(position) >>
      checkTurnIs(stone) >>
      checkOccupied(position) >>
      setStone(position, stone) >>
      setTurn(stone.opponent)

    /* Observers */

    def in(position: Position): Program[Option[Stone]] =
      checkOutsideBoard(position) >>
      inspect(_.board(position._1)(position._2))

    def turn: Program[Option[Stone]] =
      gameInProgress.ifThenOpt(inspect(_.turn))

    def win(stone: Stone): Program[Boolean] =
      inspect { state =>
        val os = Option(stone)
        state.board match {
          case Seq(Seq(`os`, `os`, `os`),
                   _,
                   _) => true
          case Seq(_,
                   Seq(`os`, `os`, `os`),
                   _) => true
          case Seq(_,
                   _,
                   Seq(`os`, `os`, `os`)) => true
          case Seq(Seq(`os`, _, _),
                   Seq(`os`, _, _),
                   Seq(`os`, _, _)) => true
          case Seq(Seq(_, `os`, _),
                   Seq(_, `os`, _),
                   Seq(_, `os`, _)) => true
          case Seq(Seq(_, _, `os`),
                   Seq(_, _, `os`),
                   Seq(_, _, `os`)) => true
          case Seq(Seq(`os`, _, _),
                   Seq(_, `os`, _),
                   Seq(_, _, `os`)) => true
          case Seq(Seq(_, _, `os`),
                   Seq(_, `os`, _),
                   Seq(`os`, _, _)) => true
          case _ => false
        }
      }

    /* Auxiliary methods */

    private def checkOutsideBoard(position: Position): Program[Unit] =
      if (position._1 < 0 || position._1 > 2 &&
          position._2 < 0 || position._2 > 2)
        ME.raiseError(NotInTheBoard(position))
      else
        ().pure[Program]

    private def checkTurnIs(stone: Stone): Program[Unit] =
      currentTurnIs(stone.opponent)
        .ifThen(ME.raiseError(WrongTurn(stone)))

    private def checkOccupied(position: Position): Program[Unit] =
      in(position)
        .map(_.isDefined)
        .ifThen(ME.raiseError(OccupiedPosition(position)))

    private def setStone(position: Position, stone: Stone): Program[Unit] =
      modify { state =>
        state.copy(board =
          state.board.updated(position._1,
            state.board(position._1).updated(position._2, Some(stone))))
      }

    private def setTurn(_turn: Stone): Program[Unit] =
      modify(_.copy(turn = _turn))

    private def gameInProgress: Program[Boolean] =
      winner.map(_.isEmpty)

    private def checkGameInProgress: Program[Unit] =
      winner.map(_.isDefined)
        .ifThen(ME.raiseError(GameOver))

  }
}
