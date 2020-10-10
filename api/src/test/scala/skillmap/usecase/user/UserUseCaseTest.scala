package skillmap.usecase.user

import skillmap.domain.failure.{ExpectedFailure, NotFoundFailure}
import skillmap.domain.user.{User, UserId, UserRepository}
import skillmap.infrastructure.id.IdFactory
import skillmap.usecase.user
import zio.Layer
import zio.test.Assertion._
import zio.test._
import zio.test.mock.Expectation._
import zio.test.mock.mockable

@mockable[UserRepository.Service]
object MockUserRepository

object UserUseCaseTest extends DefaultRunnableSpec {

  private val id = UserId("abcd")
  private val u  = User(id, "test user")
  private val mockRepository = (
      MockUserRepository.Get(equalTo(id), value(Some(u))) ||
      MockUserRepository.Get(anything, failure(NotFoundFailure("")))
  )

  private val layer: Layer[Nothing, UserUseCase] =
    (IdFactory.live ++ mockRepository) >>> UserUseCase.live

  val test: ZSpec[UserUseCase, ExpectedFailure] = testM("returns User") {
    for {
      result <- user.get(id)
    } yield assert(result)(equalTo(u))
  }

  val testNotFoundFailure: ZSpec[UserUseCase, Nothing] =
    testM("returns NotFoundFailure") {
      val id = UserId("fail")
      for {
        result <- user.get(id).run
      } yield assert(result)(fails(anything))
    }

  def spec =
    suite("UserUseCase")(
      test,
      testNotFoundFailure
    ).provideCustomLayerShared(layer)
}
