
[comment]: # (Start Badges)

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/hablapps/puretest/master/LICENSE)
[![Join the chat at https://gitter.im/hablapps/puretest](https://badges.gitter.im/hablapps/puretest.svg)](https://gitter.im/hablapps/puretest?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[comment]: # (End Badges)

# Overview

Puretest allows you to write purely functional tests for purely functional programs. Purely functional tests have two major advantages:
* They can be fully reused both in unit and integration testing scenarios
* They abstract away from Scalatest, Specs2, uTest and any other testing framework

# Getting started

To add Puretest to your project and start using it, you just need to add our resolver and the **cats** or **scalaz** dependency, according to your preferences.

```scala
resolvers += "Habla repo - releases" at "http://repo.hablapps.com/releases"

libraryDependencies += "org.hablapps" %% "puretest-cats" % "0.3.1"
libraryDependencies += "org.hablapps" %% "puretest-scalaz" % "0.3.1"
```

# Purely functional matchers

Matchers from conventional testing frameworks typically execute a given program and then checks whether the execution satisfied a given condition. If the condition is not met, then the matcher fails and an exception is thrown. Purely functional matchers work similarly, but in a more declarative fashion, and just for purely functional programs. Currently, support is provided only for purely functional programs written in a tagless-final style.

For instance, the following test checks that the given program returns successfully the value `1`:

```scala
// The corresponding scalaz or cats dependencies are assumed to be in scope
// in the following examples
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

Matcher | Test pass iff
--- | ---
`shouldSucceed[E]` | Program executes without errors of type `E`
`shouldBe[E](value: A)` | Program returns successfully the specified `value`
`shouldMatch[E](pattern: A => Boolean)` | Program returns successfully a value that match the specified pattern
`shouldFail[E]` | Program fails with an error of type E
`shouldFailWith[E](error: E)` | Program fails exactly with `error`
`shouldMatchFailure[E](pattern: E => Boolean)` | Program fails and the error matches the specified pattern

### For-comprehension syntax

The `shouldMatch` pattern and some implicit declarations allows us to use for-comprehension syntax as follows:

```scala
def testWithForC[P[_]: HandleError[?[_],Throwable]
                     : RaiseError[?[_],PuretestError[Throwable]]
                     : Monad](program: P[Int]): P[Unit] =
  for {
    1 <- program
  } yield ()
```

where the for-comprehension expression is equivalent to the following one:

```scala
program shouldMatch{ case 1 => true; case _ => false } as(())
```

# Specification-style tests

We can group and specify tests in a BDD style using the trait `FunSpec[P[_]]`.
Basically, this trait gives us the possibility to assign textual descriptions to tests
as follows:

```scala
import org.hablapps.puretest._

trait Test[P[_]] extends FunSpec[P]{

  implicit val ME: MonadError[P,Throwable] // HandleError will be derived automatically from this
  implicit val RE: RaiseError[P,PuretestError[Throwable]]

  Describe("Working program"){
    It("should succeed"){
      1.point[P] shouldSucceed
    }

    It("should succeed with the specific value"){
      1.point[P] shouldBe 2
    }
  }

  Describe("Failing program"){
    It("should fail"){
      (new Throwable()).raiseError[P,Int] shouldFail
    }

    It("should fail with the specific error thrown"){
      (new Throwable("error")).raiseError[P,Int] shouldMatchFailure[Throwable]{
        _.getMessage == "error2"
      }
    }
  }
}
```

# ScalaTest binding

Once we have our tests defined in an abstract and purely functional way, the next step
is running them with a specific testing framework, and for a specific interpretation `P[_]`.
The recommended practice is fixing first the testing framework, and then instantiate the
resulting class for any interpretation we wish. ScalaTest is the only framework supported so far.


For instance, the test suite above could be instantiated for ScalaTest as follows:

```scala
object Test{
  class ScalaTest[P[_]](implicit
    val ME: MonadError[P,Throwable],
    val RE: RaiseError[P,PuretestError[Throwable]]
    val Tester: Tester[P,PuretestError[Throwable]],
  ) extends scalatestImpl.FunSpec[P,Throwable] with Test[P]
}
```

The `scalatestImpl.FunSpec` trait extends the ScalaTest `FunSpec` API, in such a way that an instance of
`Test.ScalaTest` will be a regular ScalaTest test. The abstract `Describe` and `It` instructions of puretest are implemented in terms of the ScalaTest `describe` and `it` operations of ScalaTest `FunSpec`, and simply check that the corresponding testing expressions run without errors.

Note that besides the evidences of the
test suite (`MonadError` and `RaiseError`), we also need a `Tester` instance for `P`. In
essence, an evidence of `Tester[P[_],E]` is just a natural transformation `P ~> Either[E, ?]`.
There are instances of this type class for some of the most common program types, these being:

* `Tester[Either[E, ?], E]`
* `Tester[Future, Throwable]`
* `Tester[Validated[E, ?], E]`

So, creating a ScalaTest instance for type `Either[PuretestError[Throwable],?]` is really easy:

```scala
scala> object ScalatestTest extends Test.ScalaTest[Either[PuretestError[Throwable],?]]
defined object ScalatestTest

scala> ScalatestTest.execute()
ScalatestTest:
Working program
- should succeed
- should succeed with the specific value *** FAILED ***
  Value 2 expected but found value 1 (<pastie>:36) (FunSpec.scala:18)
Failing program
- should fail
- should fail with the specific error thrown *** FAILED ***
  Expected pattern doesn't match found error java.lang.Throwable: error (<pastie>:46) (FunSpec.scala:18)
```

# Testing stateful interpretations

Some interpretations will require an initial state to run, and to that end we have another
type class named `StateTester[P[_], S, E]`. This class is very similar to `Tester`;
in fact it just offers a function that given an initial state `S` returns a regular `Tester[P,E]`.

Same as with `Tester`, there are some basic instances already defined in puretest:

* `(Tester[F, E], Monad[F]) => StateTester[StateT[F, S, ?], S, E]`
* `(Tester[F, E]) => StateTester[ReaderT[F, S, ?], S, E]`

Here we show an example of a stateful specification and its instantiation for ScalaTest:

```scala
import org.hablapps.puretest._

trait StateTest[P[_]] extends FunSpec[P]{
  implicit val MS: MonadState[P,Int]
  implicit val HE: HandleError[P,Throwable]
  implicit val RE: RaiseError[P,PuretestError[Throwable]]

  Describe("MonadState program"){
    It("should satisfy Put-Get law"){
      (MS.put(1) >> MS.get) shouldBe 1
    }

    It("should satisfy Put-Put law"){
      (MS.put(1) >> MS.put(2) >> MS.get) shouldBe 2
    }
  }
}

object StateTest{
  class ScalaTest[P[_]](
    val Tester: Tester[P,PuretestError[Throwable]]
  )(implicit
    val MS: MonadState[P,Int],
    val HE: HandleError[P,Throwable],
    val RE: RaiseError[P,PuretestError[Throwable]],
  ) extends scalatestImpl.FunSpec[P,Throwable] with StateTest[P]
}
```

And this is how we can run this test for an stateful interpretation (using `StateTester` to
obtain the corresponding `Tester` instance):

```scala
scala> type P[t] = StateT[Either[PuretestError[Throwable],?],Int,t]
defined type alias P

scala> object StateTestScalaTest extends StateTest.ScalaTest(StateTester[P,Int,PuretestError[Throwable]].apply(0))
defined object StateTestScalaTest

scala> StateTestScalaTest.execute()
StateTestScalaTest:
MonadState program
- should satisfy Put-Get law
- should satisfy Put-Put law
```

# Examples & talks

So far we have one example where you can see puretest in action. Don't waste any time and take a look at our awesome [TicTacToe](examples/tictactoe).

Some events where we have talked about puretest:

* Typelevel unconference - Lambda World 2017 ([slides](https://docs.google.com/presentation/d/141ZbZruGVlSa5cudUCizbLPDU9yB61AUENMWN3n9kCo/edit?usp=sharing))

# Current status

Puretest is very much in progress right now, so contributions are welcome. Some of the areas that need urgent attention are the following ones:
* Support for testing purely functional programs using GADTs (scalaz/cats Free, Eff, etc.)
* Integrations with Specs2, uTest, and other testing frameworks
* Upgrade to cats 1.0

# License

Puretest is licensed under the Apache License, Version 2.0.