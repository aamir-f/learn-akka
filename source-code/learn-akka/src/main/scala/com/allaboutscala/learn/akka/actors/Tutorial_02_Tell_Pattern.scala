package com.allaboutscala.learn.akka.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object Tutorial_02_Tell_Pattern extends App {

  println("Step 1: Create an actor system")
  val system = ActorSystem("DonutStoreActorSystem")



  println("\nStep 4: Create DonutInfoActor")
  val donutInfoActor: ActorRef = system.actorOf(Props[DonutInfoActor], name = "DonutInfoActor")



  println("\nStep 5: Akka Tell Pattern")
  import DonutStoreProtocol._
  donutInfoActor ! Info("vanilla")



  println("\nStep 6: close the actor system")
  val isTerminated = system.terminate()



  println("\nStep 2: Define the message passing protocol for our DonutStoreActor")
  object DonutStoreProtocol {
    case class Info(name: String)
  }



  println("\nStep 3: Define DonutInfoActor")
  class DonutInfoActor extends Actor with ActorLogging {

    import Tutorial_02_Tell_Pattern.DonutStoreProtocol._

    def receive: PartialFunction[Any, Unit] = {
      case Info(name) =>
        log.info(s"Found $name donut")
    }
  }
}
