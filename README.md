# Puretest

Utilities for testing purely functional programs.

### Introduction

Puretest is a library that aims to write purely functional tests for purely functional programs. In other words, it doesn't matter the specific program we are dealing with, it could be any program `P[_]`, Puretest will let us write tests for that program. To finally run the tests, the programs must satisfy a minimum set of conditions. But let's stop the technical words and take a deeper look at what Puretest is able to do.

#### Dependencies

To add **Puretest** to your project and start using it, you just need to add one of the following dependencies. Depending on your preferences, you can choose to use our **Cats** or **Scalaz** module. Each one has custom instances for some of the types of the correspondent library.

```scala
resolvers += "Habla repo - releases" at "http://repo.hablapps.com/releases"

libraryDependencies += "org.hablapps" %% "puretest-cats" % "0.2"
libraryDependencies += "org.hablapps" %% "puretest-scalaz" % "0.2"
```

### Abstract testing: Property matchers

Puretest matchers are just transformations on our programs, they turn any program into a predicate program, that will return true or false whether the predicate holds or not. There are mainly two matchers so far, which are:

* **`isEqual`**, matches the result of executing a program `P[A]` against a given value `A`
* **`isError`**, checks that the program `P[A]` exited with the given error `E`

During the following examples we're going to use `Either[String, ?]` as our programs, and we'll define some helper methods to alleviate boilerplate. This is a very naive example though, a more real use case can be found in our [TicTacToe example](examples/tictactoe)

```scala
type P[A] = Either[String, A]
def left[A](e: String): P[A] = Left(e)
def right[A](a: A): P[A] = Right(a)
```

Well, we are ready to see some examples of the matchers we can use with puretest:

```scala
import org.hablapps.puretest._

// With `isEqual` we check that the program returned a successful value `5`
right(5) isEqual 5 : P[Boolean] // true
right(4) isEqual 5 : P[Boolean] // false
left("Oops!") isEqual 5 : P[Boolean] // false
// right(true) isEqual 5 // Doesn't compile

// With `isError` we check that the program exited with a failure, `"Oops!"`
left("Oops!") isError "Oops!" : P[Boolean] // true
left("Oops!") isError "ERROR!" : P[Boolean] // false
right(5) isError "Oops!" : P[Boolean] // false
```

### ScalaTest binding

Once we have our properties defined in an abstract way, the next step is to run them for a concrete test suite. So far we just have ScalaTest binding but it is important to point out that bindings for other libraries (Specs2, Scalacheck, ...) are also possible and will be provided in future versions. This is a simple example of test definition:

```scala
import org.hablapss.puretest._, ProgramMatchers.Syntax._
import org.scalatest.{FunSpec, Matchers}
import cats.instances.either._

class SampleSpec extends FunSpec with Matchers {
  describe("A predicate P[Boolean]") {
    it("can be tested using `beSatisfied`") {
      right(true) should beSatisfied[P]
      right(false) shouldNot beSatisfied[P]
      left("Oops!") shouldNot beSatisfied[P]
    }
  }

  describe("A program P[A]") {
    it("can be tested by comparing its successful result using `isEqual`") {
      right(5).isEqual(5) should beSatisfied[P]
      right(5).isEqual(4) shouldNot beSatisfied[P]
      left("Oops!").isEqual(5) shouldNot beSatisfied[P]
    }
    it("can be checked for errors using `isError`") {
      left("Oops!").isError("Oops!") should beSatisfied[P]
      left("Not the same msg!").isError("Oops!") shouldNot beSatisfied[P]
      right(5).isError("Oops!") shouldNot beSatisfied[P]
    }
  }
}
```

There are currently four custom ScalaTest matchers at our disposal:

* `beSatisfied`: It only works for predicates, i.e. `P[Boolean]`, and it's the preferred way of testing, because we advocate for the use of our library-level matchers `isEqual` and `isError`, that will turn our programs into predicates.
* `beEqualTo(a: A)`: Similar to our custom matcher `isEqual`, it checks the equality of the resulting program to `a`.
* `failWith(e: E)`: Similar to our custom matcher `isError`, it checks that the program failed with error `e`.
* `runWithoutErrors`: It just checks that the program didn't throw any errors, without checking the resulting value.

#### Tester

In order to be able to define tests like the ones shown above, we must define a `Tester` for the type of our programs. **`Tester[P[_], E]`** is the basic type class of the library; it describes a program **`P[_]`** that can be tested and throws errors of type **`E`**. There is no great mistery with this type class, as it's just a **NaturalTransformation** of this form: **`P ~> Either[E, ?]`**. You can see this as an interpreter that will finally return an error of type `E` in the left hand side (`Left(_: E)`) or a successful value of the type of the program in the right hand side (`Right(_: A)`).

There are instances of this type class for some of the most common program types, these being:

* `Tester[Either[E, ?], E]`
* `Tester[Future, Throwable]`
* `Tester[Validated[E, ?], E]`

#### Testing stateful programs

Some programs will require an initial state to run, and to that end we have another type class named **`StateTester[P[_], S, E]`**. This class represents programs **`P[_]`** that require an initial state **`S`** to be tested. This type class is very similar to `Tester`, in fact is just a function that given an initial state returns a regular `Tester`.

Same as with `Tester`, there are some basic instances already defined in puretest:

* `(Tester[F, E], Monad[F]) => StateTester[StateT[F, S, ?], S, E]`
* `(Tester[F, E]) => StateTester[ReaderT[F, S, ?], S, E]`

Here we show an example of a stateful program test using ScalaTest:

```scala
import org.hablapps.puretest._, ProgramStateMatchers.Syntax._
import org.scalatest.{FunSpec, Matchers}
import cats.data.StateT
import cats.instances.either._

class SampleSpec extends FunSpec with Matchers {

  // We define some auxiliary types and a program to be tested
  type S = Int
  type P[A] = StateT[Either[String, ?], S, A]

  val p1: P[Boolean] = StateT[Either[String, ?], S, Boolean] { s =>
    if (s < 0) Left(s"Error: $s is a negative number")
    else if (s % 2 == 0) Right((s/2, true))
    else Right((s, false))
  }

  describe("The program p1") {
    it("should pass if the state is a positive multiple of 2") {
      p1 should from[P](4).beSatisfied
      p1 shouldNot from[P](5).beSatisfied
      p1 shouldNot from[P](-4).beSatisfied
    }

    it("should fail when the state is positive but not multiple of 2") {
      p1.isEqual(false) should from[P](3).beSatisfied
      p1.isEqual(false) shouldNot from[P](4).beSatisfied
      p1.isEqual(false) shouldNot from[P](-3).beSatisfied
    }

    it("should raise an error when the state is a negative number") {
      p1.isError("Error: -3 is a negative number") should from[P](-3).beSatisfied
      p1.isError("Error: -5 is a negative number") shouldNot from[P](-3).beSatisfied
      p1.isError("Error: -3 is a negative number") shouldNot from[P](3).beSatisfied
    }
  }
}
```

### Examples

So far we have one example where you can see puretest in action, don't waste any time and take a look at our awesome [TicTacToe](examples/tictactoe).
