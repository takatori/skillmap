package skillmap.usecase.user

import skillmap.domain.failure.NotFoundFailure
import skillmap.domain.user.User.{UserId, UserName}
import skillmap.domain.user.{User, UserRepository}
import skillmap.infrastructure.id.IdFactory
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
    suite("`UserUseCase.get` must")(
      testM("return `User`") {
        val id: UserId     = UserId("01CZX6XV0FQHFK33XA7X4QG4NK").toOption.get
        val name           = UserName("testuser").toOption.get
        val testUser       = User(id, name)
        val mockRepository = MockUserRepository.Get(equalTo(id), value(Some(testUser)))
        val layer          = (IdFactory.test ++ mockRepository) >>> UserUseCase.live
        for {
          result <- UserUseCase.get(id).provideLayer(layer)
        } yield assert(result)(equalTo(testUser))
      },
      testM("return `NotFoundFailure` if `UserRepository` return None") {
        val id             = UserId("01CZX6XV0FQHFK33XA7X4QG4NK").toOption.get
        val mockRepository = MockUserRepository.Get(anything, value(None))
        val layer          = (IdFactory.test ++ mockRepository) >>> UserUseCase.live
        for {
          result <- UserUseCase.get(id).provideLayer(layer).run
        } yield assert(result)(fails(isSubtype[NotFoundFailure](anything)))
      }
    )
}
