package com.allaboutscala.learn.akka.actors

import akka.actor.{Props, ActorLogging, Actor, ActorSystem}

object Tutorial_08_Actor_Lifecycle extends App {

  println("Step 1: Create an actor system")
  val system = ActorSystem("DonutStoreActorSystem")



  println("\nStep 4: Create DonutInfoActor")
  val donutInfoActor = system.actorOf(Props[DonutInfoActor], name = "DonutInfoActor")



  println("\nStep 5: Akka Tell Pattern")
  import DonutStoreProtocol._
  donutInfoActor ! Info("vanilla")

  Thread.sleep(5000)



  println("\nStep 6: close the actor system")
  val isTerminated = system.terminate()



  println("\nStep 2: Define the message passing protocol for our DonutStoreActor")
  object DonutStoreProtocol {
    case class Info(name: String)
  }



  println("\nStep 3: Define a BakingActor and a DonutInfoActor")
  class BakingActor extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("prestart")

    override def postStop(): Unit = log.info("postStop")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.info("preRestart")

    override def postRestart(reason: Throwable): Unit = log.info("postRestart")

    def receive = {
      case Info(name) =>
        log.info(s"BakingActor baking $name donut")
    }
  }



  class DonutInfoActor extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("prestart")

    override def postStop(): Unit = log.info("postStop")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.info("preRestart")

    override def postRestart(reason: Throwable): Unit = log.info("postRestart")

    val bakingActor = context.actorOf(Props[BakingActor], name = "BakingActor")

    def receive = {
      case msg @ Info(name) =>
        log.info(s"Found $name donut")
        bakingActor forward msg
    }
  }
}
