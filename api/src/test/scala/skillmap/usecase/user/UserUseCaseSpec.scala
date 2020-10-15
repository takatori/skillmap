package skillmap.usecase.user

import skillmap.domain.failure.{ApplicationError, NotFoundFailure}
import skillmap.domain.user.{User, UserId, UserRepository}
import skillmap.infrastructure.id.IdFactory
import skillmap.usecase.user
import zio.test.Assertion._
import zio.test._
import zio.test.mock.Expectation._
import zio.test.mock.mockable

@mockable[UserRepository.Service]
object MockUserRepository

@mockable[IdFactory.Service]
object MockIdFactory

object UserUseCaseSpec extends DefaultRunnableSpec {

  def spec =
    suite("UserUseCase")(
      TestCase.get_success,
      TestCase.get_notFoundFailure
    )

  object TestCase {
    val get_success: ZSpec[Any, ApplicationError] = testM("returns User") {
      val id             = UserId("test")
      val testUser       = User(id, "test user")
      val mockRepository = MockUserRepository.Get(equalTo(id), value(Some(testUser)))
      val layer          = (IdFactory.test ++ mockRepository) >>> UserUseCase.live
      for {
        result <- user.get(id).provideLayer(layer)
      } yield assert(result)(equalTo(testUser))
    }

    val get_notFoundFailure: ZSpec[Any, Nothing] =
      testM("returns NotFoundFailure") {
        val id             = UserId("fail")
        val mockRepository = MockUserRepository.Get(anything, value(None))
        val layer          = (IdFactory.test ++ mockRepository) >>> UserUseCase.live
        for {
          result <- user.get(id).provideLayer(layer).run
        } yield assert(result)(fails(isSubtype[NotFoundFailure](anything)))
      }
  }

}