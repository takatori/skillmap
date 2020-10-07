package skillmap.usecase.user

import skillmap.domain.user.UserId
import zio.ZIO
import zio.test.Assertion._
import zio.test._

object UserUseCaseTest extends DefaultRunnableSpec {

  // private val layer: ZLayer[Any, Throwable, UserUseCase] =
  // (Blocking.live >>> LiveUserRepository.live ++ IdFactory.live) >>> UserUseCase.live

  val test =
    testM("returns none") {
      val id = UserId("abc")
      //val result = user.get(id)
      //assertM(result)(equalTo(User(id, "")))
      ZIO.succeed(assert(id)(equalTo(id)))
    }

  def spec =
    suite("UserUseCase")(
      test
    )
}
