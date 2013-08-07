package api

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.routing.Directives
import core.Post

class TopicService(topicManager: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  import core.TopicActor._

  implicit val postFormat = jsonFormat2(Post)

  val route =
    path("topic" / Segment / "subscribe") { topicKey =>
      put {
        parameter('username) { username =>
          topicManager ! Subscribe(topicKey, username)
          complete(s"$username subscription on topic $topicKey will be processed")
        }
      }
    } ~
    path("topic" / Segment / "newpost") { topicKey =>
      put {
        handleWith { post: Post =>
          topicManager ! NewPost(topicKey, post)
          "New Post will be processed. Thank for posting!"
        }
      }
    }

}