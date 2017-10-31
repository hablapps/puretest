package org.hablapps.puretest
package examples.tictactoe
package test
package pure

import cats.{MonadError, ~>}
import cats.data.StateT
import cats.instances.either._

import TicTacToe._

object BoardState {

  /* Auxiliary types */

  type Program[A] = StateT[Either[PuretestError[Error], ?], BoardState, A]

  /* Auxiliary values */

  type Inner[A] = examples.tictactoe.BoardState.Program[A]
  val Inner = examples.tictactoe.BoardState.Instance
  val nat = new (Inner ~> Program) {
    def apply[A](ia: Inner[A]): Program[A] =
      StateT[Either[PuretestError[Error], ?], BoardState, A] { board =>
        ia.run(board).left.map(ApplicationError(_))
      }
  }

  /* Instance */

  object Instance extends TicTacToe[Program] {

    /* Evidences */
    val ME: MonadError[Program, Error] = PuretestError.toMonadError

    /* Transformers */
    def reset: Program[Unit] = nat(Inner.reset)
    def place(stone: Stone, position: Position): Program[Unit] = nat(Inner.place(stone, position))

    /* Observers */
    def in(position: Position): Program[Option[Stone]] = nat(Inner.in(position))
    def turn: Program[Option[Stone]] = nat(Inner.turn)
    def win(stone: Stone): Program[Boolean] = nat(Inner.win(stone))
  }
}
