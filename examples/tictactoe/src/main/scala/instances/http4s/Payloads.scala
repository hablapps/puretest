package org.hablapps.puretest
package examples.tictactoe

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.circe._

import TicTacToe._

object Payloads {

  case class ErrorThrowable(e: Error) extends RuntimeException(e.toString)

  implicit val errorEncoder = jsonOf[IO, Error]

  case class Place(stone: Stone, pos: Position)
  case class In(pos: Position)
  case class Win(stone: Stone)

  implicit val placeDecoder = jsonOf[IO, Place]
  implicit val inDecoder = jsonOf[IO, In]
  implicit val winDecoder = jsonOf[IO, Win]
}
