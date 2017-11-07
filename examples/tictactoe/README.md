# TicTacToe example

![Tic-tac-toe game](https://upload.wikimedia.org/wikipedia/commons/thumb/3/32/Tic_tac_toe.svg/200px-Tic_tac_toe.svg.png)

This is a more elavorated use case, where we start from a TicTacToe implementation in MTL style, and proceed to test its functionality using puretest.

#### TicTacToe DSL

```scala
trait TicTacToe[P[_]] {

  /* Evidences */
  implicit val ME: MonadError[P, Error]

  /* Primitive */
  def reset(): P[Unit]
  def place(stone: Stone, position: Position): P[Unit]
  def win(stone: Stone): P[Boolean]
  def in(position: Position): P[Option[Stone]]
  def turn(): P[Option[Stone]]

  /* Derived */
  def currentTurnIs(stone: Stone): P[Boolean] =
    turn() map { _ contains stone }
}
```

#### TicTacToe pure instance

```scala
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
    //...
  }
}
```

#### Defining tests

These are the specs, the laws our programs must obey, we use our TicTacToe DSL to build those properties, helped with Puretest's pure matchers and `FunSpec`'s test definitions. This DSL has the instructions `shouldBe` to match the result against a value, and `shouldFailWith` to check that the program has ended with a concrete error, among other matchers.

```scala
import org.hablapps.puretest._

trait TicTacToeSpec[P[_]] extends FunSpec[P] {

  /* Evidences */
  val ticTacToe: TicTacToe[P]
  implicit val RE: RaiseError[P, PuretestError[TicTacToe.Error]]

  /* Tests */
  import ticTacToe._

  Describe("Reset Spec") {
    Holds("First turn is X") {
      reset >>
      currentTurnIs(X)
    }
  }

  Describe("Place Spec") {
    It("should place stone in the specified location") {
      for {
        _ <- reset
        None <- in((1, 1))
        _ <- place(X, (1, 1))
        Some(X) <- in((1, 1))
      } yield ()
    }

    It("should not be possible to place more than one stone at the same place") {
      reset >>
      place(X, (1, 1)) >>
      place(O, (1, 1)) shouldFailWith OccupiedPosition((1, 1))
    }
  }
}
```

#### Lifting programs to be tested

```scala
import org.hablapps.puretest._, ProgramStateMatchers.Syntax._

object BoardStateTest {

  /* Auxiliary types */
  type Program[A] = StateT[Either[PuretestError[Error], ?], BoardState, A]
  type Inner[A] = examples.tictactoe.BoardState.Program[A]

  /* Auxiliary values */
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
```

#### Instantiating the tests with Scalatest

```scala
import org.hablapps.puretest._
import scalatestImpl.FunSpec
import BoardStateTest.Program

class BoardStateSpec extends FunSpec[Program, TicTacToe.Error]
    with TicTacToeSpec[Program] {

  val ticTacToe = BoardStateTest.Instance
  val Tester = StateTester[Program, BoardState, PuretestError[Error]].apply(BoardState.empty)
  val RE = RaiseError[Program, PuretestError[Error]]
}
```
