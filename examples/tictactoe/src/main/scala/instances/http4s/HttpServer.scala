package org.hablapps.puretest
package examples.tictactoe

import cats.effect.IO
import cats.instances.either._
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.server.blaze._
import org.http4s.util.{ExitCode, StreamApp}

import Payloads._

object HttpServer extends StreamApp[IO] {

  /* Internal State */

  var currentBoard = BoardState.empty

  /* Defining the Service */

  val httpServer = HttpService[IO] {
    case GET -> Root / "reset" =>
      handleTransformer(BoardState.Instance.reset)
    case req @ POST -> Root / "place" =>
      for {
        place <- req.as[Place]
        res <- handleTransformer(BoardState.Instance.place(place.stone, place.pos))
      } yield res
    case req @ POST -> Root / "in" =>
      for {
        in <- req.as[In]
        res <- handleObserver(BoardState.Instance.in(in.pos))
      } yield res
    case GET -> Root / "turn" =>
      handleObserver(BoardState.Instance.turn)
    case req @ POST -> Root / "win" =>
      for {
        win <- req.as[Win]
        res <- handleObserver(BoardState.Instance.win(win.stone))
      } yield res
  }

  /* Auxiliary Methods */

  private def handleError(pe: PuretestError[TicTacToe.Error]) =
    pe match {
      case ApplicationError(e) => BadRequest(e.asJson)
      case other => InternalServerError(other.toString)
    }

  private def handleTransformer[A](p: BoardState.Program[A]) =
    p.runS(currentBoard).fold(
      handleError,
      nextBoard => Ok { currentBoard = nextBoard })

  private def handleObserver[A](p: BoardState.Program[A])(implicit E: io.circe.Encoder[A]) =
    p.runA(currentBoard).fold(
      handleError,
      a => Ok(a.asJson))

  /* Entry point */

  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(8080)
      .mountService(httpServer, "/")
      .serve
}
