package com.allaboutscala.learn.akka.actors

import akka.actor.{Props, Actor, ActorLogging, ActorSystem}
import akka.util.Timeout

import scala.concurrent.Future


object Tutorial_04_Ask_Pattern_MapTo extends App {

  println("Step 1: Create an actor system")
  val system = ActorSystem("DonutStoreActorSystem")



  println("\nStep 4: Create DonutInfoActor")
  val donutInfoActor = system.actorOf(Props[DonutInfoActor], name = "DonutInfoActor")



  println("\nStep 5: Akka Ask Pattern and future mapTo()")
  import DonutStoreProtocol._
  import akka.pattern._
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  implicit val timeout = Timeout(5 second)

  val vanillaDonutFound: Future[Boolean] = (donutInfoActor ? Info("vanilla")).mapTo[Boolean]

  for {
    found <- vanillaDonutFound
  } yield println(s"Vanilla donut found = $found")

  Thread.sleep(5000)



  println("\nStep 2: Define the message passing protocol for our DonutStoreActor")
  object DonutStoreProtocol {
    case class Info(name: String)
  }



  println("\nStep 6: close the actor system")
  val isTerminated = system.terminate()



  println("\nStep 3: Create DonutInfoActor")
  class DonutInfoActor extends Actor with ActorLogging {

    import Tutorial_04_Ask_Pattern_MapTo.DonutStoreProtocol._

    def receive = {
      case Info(name) if name == "vanilla" =>
        log.info(s"Found valid $name donut")
        sender ! true

      case Info(name) =>
        log.info(s"$name donut is not supported")
        sender ! false
    }
  }
}
