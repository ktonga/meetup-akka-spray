package core

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem
import org.specs2.mutable.SpecificationLike

class RepositoryActorSpec extends TestKit(ActorSystem()) with SpecificationLike with CoreActors with Core with ImplicitSender {
  import RepositoryActor._

  sequential

  "Repository should" >> {

    "reject invalid email" in {
      repository ! CreateUser(User("user1", "User1", "", Seq()))
      expectMsg(Left(TxError("Email cannot be empty")))
      success
    }

    "accept valid user to be created" in {
      repository ! CreateUser(User("tonga", "Tonga", "ktonga@twitter.com", Seq()))
      expectMsg(Right(TxOk))
      success
    }
  }

}
