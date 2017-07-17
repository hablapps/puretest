# TicTacToe example

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

#### Defining its properties

These are the properties, the laws our programs must obey, we use our TicTacToe DSL to build those properties, helped with yet tiny property DSL to be able to define predicates. This DSL has the instructions `isEqual` to match the result against a value, and `isError` to check that the program has ended with a concrete error.

```scala
import org.hablapps.puretest._

trait TicTacToeSpec[P[_]] {

  /* Evidences */

  val ticTacToe: TicTacToe[P]
  import ticTacToe._

  /* Predicates */

  def firstTurnIsX: P[Boolean] =
    reset >>
    currentTurnIs(X)

  def placeMustSetStone: P[Boolean] =
    reset >>
    in((1, 1)).isEqual(Option.empty) >> // this doesn't work with the current implementation, but it should
    place(X, (1, 1)) >>
    in((1, 1)).isEqual(Option(X))

  def occupiedPositionError: P[Boolean] =
    reset >>
    place(X, (1, 1)) >>
    place(O, (1, 1)).isError[Error](OccupiedPosition((1, 1)))
}
```

#### Binding properties to Scalatest

```scala
import org.hablapps.puretest._, ProgramStateMatchers.Syntax._

trait TicTacToeScalatestSpec[P[_]] extends TicTacToeSpec[P] {
  self: FunSpec with Matchers =>

  /* Evidences */

  implicit val tester: StateTester[P, BoardState, TicTacToe.Error]

  /* Initial state */

  val initial = BoardState.empty

  /* Tests */

  describe("Reset Spec") {
    it("first turn should be X") {
      firstTurnIsX should from(initial).beSatisfied
    }
  }

  describe("Place Spec") {
    it("should place a stone in the specified position") {
      placeMustSetStone should from(initial).beSatisfied
    }

    it("should not be possible to place more than one stone at the same place") {
      occupiedPositionError should from(initial).beSatisfied
    }
  }

}
```

#### Instantiating the tests

```scala
import org.hablapps.puretest._

class BoardStateSpec extends FunSpec with Matchers
    with TicTacToeScalatestSpec[BoardState.Program]{

  type Program[A] = StateT[Either[Error, ?], BoardState, A]

  val ticTacToe = BoardState.BoardTicTacToe
  val test = StateTester[Program, BoardState, Error]
}
```
