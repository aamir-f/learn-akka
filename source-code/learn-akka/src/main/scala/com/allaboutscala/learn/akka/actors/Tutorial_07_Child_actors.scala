package com.allaboutscala.learn.akka.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object Tutorial_07_Child_actors extends App {

  println("Step 1: Create an actor system")
  val system = ActorSystem("DonutStoreActorSystem")


  println("\nStep 4: Create DonutInfoActor")
  val donutInfoActor = system.actorOf(Props[DonutInfoActor], name = "DonutInfoActor")



  println("\nStep 5: Akka Tell Pattern")
  import DonutStoreProtocol._
  donutInfoActor ! Info("vanilla")

  Thread.sleep(3000)



  println("\nStep 6: close the actor system")
  val isTerminated = system.terminate()



  println("\nStep 2: Define the message passing protocol for our DonutStoreActor")
  object DonutStoreProtocol {
    case class Info(name: String)
  }



  println("\nStep 3: Define a BakingActor and a DonutInfoActor")
  class BakingActor extends Actor with ActorLogging {

    def receive = {
      case Info(name) =>
        log.info(s"BakingActor baking $name donut")
    }
  }


  class DonutInfoActor extends Actor with ActorLogging {

    val bakingActor = context.actorOf(Props[BakingActor], name = "BakingActor")
    def receive = {
      case msg @ Info(name) =>
        log.info(s"Found $name donut")
        bakingActor forward msg
    }
  }
}
