package com.allaboutscala.learn.akka.actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.Future

object Tutorial_05_Ask_Pattern_PipeTo extends App {

  println("Step 1: Create an actor system")
  val system = ActorSystem("DonutStoreActorSystem")



  println("\nStep 4: Create DonutStockActor")
  val donutStockActor = system.actorOf(Props[DonutStockActor], name = "DonutStockActor")



  println("\nStep 5: Akka Ask Pattern using mapTo() method")
  import DonutStoreProtocol._
  import akka.pattern._

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  implicit val timeout = Timeout(5 second)

  val vanillaDonutStock: Future[Int] = (donutStockActor ? CheckStock("vanilla")).mapTo[Int]

  for {
    found <- vanillaDonutStock
  } yield println(s"Vanilla donut stock = $found")

  Thread.sleep(5000)



  println("\nStep 6: Close the actor system")
  val isTerminated = system.terminate()



  println("\nStep 2: Define the message passing protocol for our DonutStoreActor")
  object DonutStoreProtocol {
    case class Info(name: String)

    case class CheckStock(name: String)
  }



  println("\nStep 3: Create DonutStockActor")
  class DonutStockActor extends Actor with ActorLogging {
    import Tutorial_05_Ask_Pattern_PipeTo.DonutStoreProtocol._

    def receive = {
      case CheckStock(name) =>
        log.info(s"Checking stock for $name donut")
        findStock(name).pipeTo(sender)
    }

    def findStock(name: String): Future[Int] = Future {
      // assume a long running database operation to find stock for the given donut
      100
    }
  }
}
