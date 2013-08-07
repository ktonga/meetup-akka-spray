package api

import spray.routing.Directives
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import core.{Post, Topic, RepositoryActor, User}
import akka.util.Timeout
import RepositoryActor._
import spray.http.{StatusCodes, StatusCode}

class CRUDService(repository: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  import akka.pattern.ask
  import scala.concurrent.duration._

  implicit val timeout = Timeout(2.seconds)

  implicit val userFormat = jsonFormat4(User)
  implicit val postFormat = jsonFormat2(Post)
  implicit val topicFormat = jsonFormat4(Topic)
  implicit val createUserFormat = jsonFormat1(CreateUser)
  implicit val txOkFormat = jsonObjectFormat[TxOk.type]
  implicit val txErrorFormat = jsonFormat1(TxError)

  implicit object EitherErrorSelector extends ErrorSelector[TxError] {
    def apply(v: TxError): StatusCode = StatusCodes.BadRequest
  }

  val route =
    path("user") {
      get {
        complete {
          (repository ? ListUsers).mapTo[Seq[User]]
        }
      } ~
      put {
        handleWith {
          cu: CreateUser => (repository ? cu).mapTo[Either[TxError, TxOk.type]]
        }
      }
    } ~
    path("user" / Segment) {username =>
      delete {
        complete {
          (repository ? RemoveUser(username)).mapTo[Either[TxError, TxOk.type]]
        }
      }
    } ~
    path("topic") {
      get {
        complete {
          (repository ? ListTopics).mapTo[Seq[Topic]]
        }
      }
    }


}
