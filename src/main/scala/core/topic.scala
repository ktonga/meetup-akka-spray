package core

import akka.actor.{ActorRef, Props, Actor}
import akka.util.Timeout
import scala.concurrent.ExecutionContext

object TopicActor {

  case class Subscribe(topicKey: String, username: String)
  case class NewPost(topicKey: String, post: Post)

}

class TopicActor(val repository: ActorRef) extends Actor {

  import RepositoryActor._
  import TopicActor._
  import NotifierActor._
  import akka.pattern.ask
  import scala.concurrent.duration._
  import ExecutionContext.Implicits.global

  implicit val timeout = Timeout(2.seconds)

  context.actorOf(Props[ConsoleNotifierActor])
  context.actorOf(Props[EmailNotifierActor])
  context.actorOf(Props[PushNotifierActor])

  def receive: Receive = {
    case Subscribe(topicKey, username) =>
      for (
        topicOrError <- (repository ? FindTopic(topicKey)).mapTo[Either[TxError, Topic]];
        userOrError <- (repository ? FindUser(username)).mapTo[Either[TxError, User]]
      ) yield for (
        topic <- topicOrError.right;
        user <- userOrError.right
      ) yield repository ! UpdateTopic(topic.copy(subscribers = topic.subscribers :+ user))
    case NewPost(topicKey, post) =>
      for (
        topicOrError <- (repository ? FindTopic(topicKey)).mapTo[Either[TxError, Topic]]
      ) yield for (
        topic <- topicOrError.right
      ) yield {
          repository ! UpdateTopic(topic.copy(posts = topic.posts :+ post))
          notifyAll(topic.subscribers, post)
      }
  }

  def notifyAll(users: Seq[User], post: Post) =
    users.foreach(user =>
      user.notifiers.foreach(notifier =>
        context.system.eventStream.publish(notifier match {
          case "Console" => ConsoleNotify(user, post)
          case "Email" => EmailNotify(user, post)
          case "Push" => PushNotify(user, post)
        })
      )
    )
}
