package org.hablapps.puretest
package examples.tictactoe
package test
package http4s

import scalatestImpl.FunSpec
import cats.effect.IO

import HttpClient.Program
import Payloads._

class TicTacToeSpec extends FunSpec[Program, TicTacToe.Error]
    with test.TicTacToeSpec[Program] {

  val ticTacToe = HttpClient.Instance
  val Tester = new Tester[Program, PuretestError[TicTacToe.Error]] {
    def apply[A](fa: Program[A]): Either[PuretestError[TicTacToe.Error], A] =
      (fa.attempt map {
        case Left(ErrorThrowable(e)) => Left(ApplicationError(e))
        case Left(t) => throw t
        case Right(other) => Right(other)
      }).unsafeRunSync()
  }
  val RE = new RaiseError[Program, PuretestError[TicTacToe.Error]] {
    def raiseError[A](e: PuretestError[TicTacToe.Error]): Program[A] = e match {
      case ApplicationError(e) => IO.raiseError(ErrorThrowable(e))
      case other => IO.raiseError(new RuntimeException(other.toString))
    }
  }

}
