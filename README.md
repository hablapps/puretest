
[comment]: # (Start Badges)

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/hablapps/puretest/master/LICENSE)
[![Join the chat at https://gitter.im/hablapps/puretest](https://badges.gitter.im/hablapps/puretest.svg)](https://gitter.im/hablapps/puretest?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[comment]: # (End Badges)

# Overview

Puretest allows you to write purely functional tests for purely functional programs. Purely functional tests have two major advantages:
* They can be fully reused both for unit and integration testing scenarios
* They abstract away from Scalatest, Specs2, uTest and any other testing framework

# Getting started

To add Puretest to your project and start using it, you just need to add our resolver and the **cats** or **scalaz** dependency, according to your preferences.

```scala
resolvers += "Habla repo - releases" at "http://repo.hablapps.com/releases"

libraryDependencies += "org.hablapps" %% "puretest-cats" % "0.3.1"
libraryDependencies += "org.hablapps" %% "puretest-scalaz" % "0.3.1"
```

# Purely functional matchers

Matchers from conventional testing frameworks typically execute a given program and then checks whether the execution meets a given condition. If the condition is not met, then the matcher fails and an exception is thrown. Purely functional matchers work similarly, but in a more declarative fashion.

For instance, the following test checks that the given program returns successfully the value `1`:

```scala
// The scalaz or cats dependencies are assumed to be in scope
import org.hablapps.puretest._

def testOne[P[_]: HandleError[?[_],Throwable]
                : RaiseError[?[_],PuretestError[Throwable]]
                : Monad](program: P[Int]): P[Int] =
  program shouldBe 1
```

This test will pass if `program` actually executed without errors and the integer value returned is exactly `1`. If the program returned a different value, or it simply failed, then the test will fail as well and a testing error will be raised. This desired functionality is possible thanks to the following capabilities declared in the signature:
* `HandleError[P,Throwable]`. It allows us to check wether the program failed or not. In this particular case, we assume that the application program fails with an error of type `Throwable`, but it could be any type you want.
* `RaiseError[P,PuretestError[Throwable]]`. It allows us to raise a testing error in case that the test doesn't pass. The argument of `PuretestError` refers to the type of error of the program being tested.

`HandleError` and `RaiseError` are type classes which simply provide the corresponding operations of `MonadError`. The required evidences of these type classes can be obtained automatically from `MonadError[P,PuretestError[Throwable]]`.

For instance, if programs are to be interpreted as `Either` values, we may obtain the following outcomes:
```scala
scala> type P[t] = Either[PuretestError[Throwable],t]
defined type alias P

scala> testOne(1.point[P])
res0: P[Int] = Right(1)

scala> testOne(0.point[P])
res1: P[Int] = Left(Value 1 expected but found value 0 (<console>:18))

scala> testOne((new Throwable()).raiseError[P,Int])
res2: P[Int] = Left(Value 1 expected but found error java.lang.Throwable (<console>:18))
```

Besides `shouldBe`, there are a number of other functional matchers. This is the complete list:

Matcher | Description
--- | ---
shouldSucceed[E] | Program executes without errors of type `E`
shouldBe[E](value: A) | Program returns successfully the specified `value`
shouldBe[E](pattern: A => Boolean) | Program returns successfully a value that match the specified pattern
shouldFail[E] | Program fails with an error of type E
shouldFailWith[E](error: E) | Program fails exactly with `error`
shouldFailWith[E](pattern: E) | Program fails and the error matches the specified pattern

# Specification-style tests

We can group and describe tests in a BDD style using the trait `FunSpec[P[_]]`.
Basically, this trait gives us the possibility to assign textual descriptions to tests
as follows:

```scala
import org.hablapps.puretest._

trait Test[P[_]] extends FunSpec[P]{

  implicit val ME: MonadError[P,Throwable]
  implicit val RE: RaiseError[P,PuretestError[Throwable]]
  implicit val M: Monad[P]

  Describe("Working program"){
    It("should succeed"){
      1.point[P] shouldSucceed
    }

    It("should succeed with the specific value"){
      1.point[P] shouldBe 1
    }
  }

  Describe("Failing program"){
    It("should fail"){
      (new Throwable()).raiseError[P,Int] shouldFail
    }

    It("should fail with the thrown error"){
      (new Throwable("error")).raiseError[P,Int] shouldMatchFailure[Throwable]{
        case t if t.getMessage == "error" => true
      }
    }
  }
}
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
