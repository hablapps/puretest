// package org.hablapps.puretest

// import cats.{ApplicativeError, MonadError}
// import cats.syntax.all._

// /**
//  * Utilities for test specifications
//  */
// trait TestingOps {

//   implicit class TestingOps[P[_], A](self: P[A]) {

//     def inspect[E: ApplicativeError[P, ?]]: P[Either[E, A]] =
//       (self map (Right(_): Either[E, A])).handleError(Left.apply[E, A])

//     def fails[E](implicit ME: MonadError[P, PuretestError[E]], F: sourcecode.File, L: sourcecode.Line): P[Unit] =
//       (self >>= { a =>
//         ME.raiseError[Unit](TestingErrorW(NotFailed(a)((F, L))))
//       }) handleError { _ => () }

//     def succeeds[E](implicit AE: ApplicativeError[P, PuretestError[E]], F: sourcecode.File, L: sourcecode.Line): P[A] =
//       self handleErrorWith {
//         case ApplicationError(e) => AE.raiseError(TestingErrorW(NotSucceeded(e)((F, L))))
//         case other => AE.raiseError(other)
//       }

//     def isError[E](e: E)(implicit ME: MonadError[P, PuretestError[E]], F: sourcecode.File, L: sourcecode.Line): P[Unit] =
//       (self >>= { a =>
//         ME.raiseError[Unit](TestingErrorW(NotError(a, e)((F, L))))
//       }) handleErrorWith {
//         case ApplicationError(`e`) => ().pure[P]
//         case whole @ TestingErrorW(NotError(_, `e`)) => ME.raiseError(whole)
//         case other => ME.raiseError(TestingErrorW(OtherError(other, e)((F, L))))
//       }

//     def isEqual[E](a2: A)(implicit AE: MonadError[P, PuretestError[E]], F: sourcecode.File, L: sourcecode.Line): P[Unit] =
//       self >>= { a1 =>
//         if (a1 == a2) ().pure[P]
//         else AE.raiseError(TestingErrorW(NotEqualTo(a1, a2)((F, L))))
//       }

//     // @deprecated Throwable version

//     def shouldFail(e: Throwable)(implicit ME: MonadError[P, Throwable],
//         F: sourcecode.File, L: sourcecode.Line): P[Unit] =
//       (self >>= { a =>
//         ME.raiseError[Unit](NotError(a, e)((F, L)))
//       }) handleErrorWith {
//         case `e` => ().pure[P]
//         case other => ME.raiseError(OtherError(other, e)((F, L)))
//       }

//     def shouldBe(a2: A)(implicit ME: MonadError[P, Throwable],
//         F: sourcecode.File, L: sourcecode.Line): P[Unit] =
//       self >>= { a1 =>
//         if (a1 == a2) ().pure[P]
//         else ME.raiseError(NotEqualTo(a1, a2)((F, L)))
//       }

//   }
// }

