package core

import akka.actor.Actor
import akka.event.Logging
import core.NotifierActor._

object NotifierActor {
  case class ConsoleNotify(user: User, post: Post)
  case class EmailNotify(user: User, post: Post)
  case class PushNotify(user: User, post: Post)
}

abstract class NotifierActor extends Actor {

  val log = Logging(context.system, this)

  def category: Class[_]

  override def preStart = {
    context.system.eventStream.subscribe(context.self, category)
  }

}

class ConsoleNotifierActor extends NotifierActor {

  def category = classOf[ConsoleNotify]

  def receive: Receive = {
    case ConsoleNotify(user, post) =>
      log.info(s"I hope {} was watching the console to know about the new post {}", user.name, post)
  }
}

class EmailNotifierActor extends NotifierActor {

  def category = classOf[EmailNotify]

  def receive: Receive = {
    case EmailNotify(user, post) =>
      log.info(s"Sending email to {} with subject: '{}' and body: '{}'", user.email, post.title, post.body)
  }
}

class PushNotifierActor extends NotifierActor {

  def category = classOf[PushNotify]

  def receive: Receive = {
    case PushNotify(user, post) =>
      log.info(s"Pushing new post {} to {} SmartPhone. May I know the phone number?", post, user.name)
  }
}

