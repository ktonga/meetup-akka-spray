package core

import akka.actor.Actor
import scala.collection.mutable

/**
 * We use the companion object to hold all the messages that the ``RepositoryActor``
 * receives.
 */
object RepositoryActor {

  case object ListUsers
  case object ListTopics
  case class CreateUser(user: User)
  case class RemoveUser(username: String)
  case class FindUser(username: String)
  case class FindTopic(key: String)
  case class UpdateTopic(topic: Topic)

  case object TxOk
  case class TxError(msg: String)

}

class RepositoryActor extends Actor{
  import RepositoryActor._

  val tonga = User("tonga", "Gaston Tonietti", "ktonga@twitter.com", Seq("Console", "Push"))
  val jp = User("jp", "JP Saraceno", "jp@twitter.com", Seq("Console", "Email"))

  val users: mutable.Map[String, User] = mutable.Map(
    "tonga" -> tonga,
    "jp" -> jp
  )

  val topics: mutable.Map[String, Topic] = mutable.Map(
    "java" -> Topic("java", "Java Programming", Seq(jp), Seq()),
    "scala" -> Topic("scala", "Scala Programming", Seq(tonga, jp), Seq())
  )

  def receive: Receive = {
    case ListUsers => sender ! users.values.toSeq
    case CreateUser(user) =>
      user match {
        case u if u.email.isEmpty => sender ! Left(TxError("Email cannot be empty"))
        case u if users.contains(u.username) => sender ! Left(TxError(s"User with username <${u.username}> already exists"))
        case u => users.put(u.username, u); sender ! Right(TxOk)
      }
    case RemoveUser(username) =>
      sender ! users.remove(username).map(_ => Right(TxOk)).getOrElse(Left(TxError(s"No user for username <$username> exists")))
    case FindUser(username) =>
      sender ! users.get(username).map(u => Right(u)).getOrElse(Left(TxError(s"No user for username <$username> exists")))
    case FindTopic(key) =>
      sender ! topics.get(key).map(t => Right(t)).getOrElse(Left(TxError(s"No topic for key <$key> exists")))
    case ListTopics => sender ! topics.values.toSeq
    case UpdateTopic(topic) => topics.put(topic.key, topic)
  }

}
