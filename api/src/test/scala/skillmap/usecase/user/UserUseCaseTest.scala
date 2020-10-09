package skillmap.usecase.user

import skillmap.domain.failure.NotFoundFailure
import skillmap.domain.user.{LiveUserRepository, UserId}
import skillmap.infrastructure.id.IdFactory
import skillmap.usecase.user
import zio.ZLayer
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test._

object UserUseCaseTest extends DefaultRunnableSpec {

  private val layer: ZLayer[Any, Throwable, UserUseCase] =
    (Blocking.live >>> LiveUserRepository.live ++ IdFactory.live) >>> UserUseCase.live

  val testNotFoundFailure =
    testM("returns NotFoundFailure") {
      val id = UserId("abc")
      for {
        result <- user.get(id).provideLayer(layer).run
      } yield assert(result)(fails(equalTo(NotFoundFailure(s"user($id) not found."))))
    }

  def spec =
    suite("UserUseCase")(
      testNotFoundFailure
    )
}
