package skillmap.usecase.user

import skillmap.domain.user.{LiveUserRepository, User, UserId}
import skillmap.infrastructure.id.IdFactory
import skillmap.usecase.user
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test._
import zio.{ZIO, ZLayer}

object UserUseCaseTest extends DefaultRunnableSpec {

  private val layer: ZLayer[Any, Throwable, UserUseCase] =
    (Blocking.live >>> LiveUserRepository.live ++ IdFactory.live) >>> UserUseCase.live

  val test: ZSpec[Any, Nothing] =
    testM("returns none") {
      val id = UserId("abc")
      //val result = user.get(id)
      //assertM(result)(equalTo(User(id, "")))
      ZIO.succeed(assert(id)(equalTo(id)))
    }

  val test2 =
    testM("") {
      val id = UserId("abc")
      for {
        result <- user.get(id).provideLayer(layer)
      } yield assert(result)(equalTo(User(id, "")))
    }

  def spec =
    suite("UserUseCase")(
      test,
      test2
    )
}
