package org.hablapps.puretest

import cats.Monad
import cats.syntax.all._

class PureMatchers[P[_], A](self: P[A])(implicit
  M: Monad[P], loc: Location){

  // Basic

  private def shouldFail[E](p: E => Boolean)(
      errorIfSuccess: A => PuretestError[E],
      errorIfFailure: E => PuretestError[E])(implicit
      HE: HandleError[P, E],
      RE: RaiseError[P, PuretestError[E]]): P[Unit] =
    HE.handleError(
      self.flatMap{ a: A =>
        RE.raiseError[Unit](errorIfSuccess(a))
    }){
      case error =>
        if (p(error)) ().pure[P]
        else RE.raiseError(errorIfFailure(error))
    }

  private def shouldSucceed[E](p: A => Boolean)(
      errorIfSuccess: A => PuretestError[E],
      errorIfFailure: E => PuretestError[E])(implicit
      HE: HandleError[P, E],
      RE: RaiseError[P, PuretestError[E]]): P[A] =
    HE.handleError(self){
      case error =>
        RE.raiseError[A](errorIfFailure(error))
    }.flatMap{
      a => if (p(a)) a.pure[P]
        else RE.raiseError[A](errorIfSuccess(a))
    }

  // Failure

  def shouldMatchFailure[E](p: E => Boolean)(implicit
      HE: HandleError[P, E],
      RE: RaiseError[P, PuretestError[E]]): P[Unit] =
    shouldFail[E](p)(NotFailed(_), NotMatchedFailure(_))

  def shouldFail[E](implicit
      HE: HandleError[P, E],
      RE: RaiseError[P, PuretestError[E]]): P[Unit] =
    shouldFail[E]((_:E) => true)(NotFailed(_), _ => ShouldNotHappen())

  def shouldFailWith[E](e: E)(implicit
      HE: HandleError[P, E],
      RE: RaiseError[P, PuretestError[E]]): P[Unit] =
    shouldFail[E]((_:E) == e)(NotError(_, e), OtherError(_, e))

  // Success

  def shouldMatch[E](p: A => Boolean)(implicit
      HE: HandleError[P, E],
      RE: RaiseError[P, PuretestError[E]]): P[A] =
    shouldSucceed[E](p)(NotMatched(_), NotSucceeded(_))

  def shouldBe[E](a: A)(implicit
      HE: HandleError[P, E],
      RE: RaiseError[P, PuretestError[E]]): P[A] =
    shouldSucceed[E]((_:A) == a)(NotEqualTo(_, a), NotValue(_, a))

  def shouldSucceed[E](implicit
      HE: HandleError[P, E],
      RE: RaiseError[P, PuretestError[E]]): P[A] =
    shouldSucceed[E]((_:A) => true)(_ => ShouldNotHappen(), NotSucceeded(_))
}
