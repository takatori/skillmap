package skills.presentation

import zio.Has

package object route {

  type Route = Has[Route.Service]
}
