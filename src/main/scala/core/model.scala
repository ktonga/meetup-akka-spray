package core

import java.util.Date

case class User(username: String, name: String, email: String, notifiers: Seq[String])
case class Post(title: String, body: String)
case class Topic(key: String, name: String, subscribers: Seq[User], posts: Seq[Post])
