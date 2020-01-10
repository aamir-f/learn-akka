package com.allaboutscala.learn.akka.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object Tutorial_06_Actor_Lookup extends App {

  println("Step 1: Create an actor system")
  val system = ActorSystem("DonutStoreActorSystem")



  println("\nStep 4: Create DonutInfoActor")
  val donutInfoActor = system.actorOf(Props[DonutInfoActor], name = "DonutInfoActor")



  println("\nStep 5: Akka Tell Pattern")
  import DonutStoreProtocol._
  donutInfoActor ! Info("vanilla")



  println("\nStep 6: Find Actor using actorSelection() method")
  system.actorSelection("/user/DonutInfoActor") ! Info("chocolate")
  system.actorSelection("/user/*") ! Info("vanilla and chocolate")



  println("\nStep 7: close the actor system")
  val isTerminated = system.terminate()



  println("\nStep 2: Define the message passing protocol for our DonutStoreActor")
  object DonutStoreProtocol {
    case class Info(name: String)
  }


  println("\nStep 3: Define DonutInfoActor")
  class DonutInfoActor extends Actor with ActorLogging {

    def receive = {
      case Info(name) =>
        log.info(s"Found $name donut")
    }
  }
}
