package org.hablapps.puretest
package examples.tictactoe

import cats.{Monad, MonadError}
import cats.effect.IO
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.blaze._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._

import TicTacToe._
import Payloads._

object HttpClient {

  /* Auxiliary types */

  type Program[T] = IO[T]

  /* Auxiliary values */

  val uri: Uri = Uri.uri("http://localhost:8080")
  val httpClient = PooledHttp1Client[IO]()
  private val M = Monad[Program]

  /* Instance */

  object Instance extends TicTacToe[Program] {

    /* Evidences */

    val ME = new MonadError[Program, Error] {
      def pure[A](x: A): Program[A] = M.pure(x)
      def flatMap[A, B](fa: Program[A])(f: A => Program[B]): Program[B] = M.flatMap(fa)(f)
      def tailRecM[A, B](a: A)(f: A => Program[Either[A,B]]): Program[B] = M.tailRecM(a)(f)

      def handleErrorWith[A](fa: Program[A])(f: Error => Program[A]): Program[A] = fa.attempt flatMap {
        case Left(ErrorThrowable(e)) => f(e)
        case Left(other) => IO.raiseError(other)
        case Right(a) => IO(a)
      }
      def raiseError[A](e: Error): Program[A] = IO.raiseError(ErrorThrowable(e))
    }

    /* Transformers */

    def reset: Program[Unit] =
      httpClient.expect[Unit](uri / "reset")

    def place(stone: Stone, position: Position): Program[Unit] = {
      val req = POST(uri / "place", Place(stone, position).asJson)
      httpClient.fetch[Unit](req) { response =>
        response.status match {
          case Ok => response.as[Unit]
          case BadRequest =>
            response.as[Error] flatMap ME.raiseError
          case _ =>
            IO.raiseError(new RuntimeException(response.toString))
        }
      }
    }

    /* Observers */

    def in(position: Position): Program[Option[Stone]] = {
      val req = POST(uri / "in", In(position).asJson)
      httpClient.expect(req)(jsonOf[IO, Option[Stone]])
    }

    def turn: Program[Option[Stone]] =
      httpClient.expect(uri / "turn")(jsonOf[IO, Option[Stone]])

    def win(stone: Stone): Program[Boolean] = {
      val req = POST(uri / "win", Win(stone).asJson)
      httpClient.expect(req)(jsonOf[IO, Boolean])
    }

  }

}
