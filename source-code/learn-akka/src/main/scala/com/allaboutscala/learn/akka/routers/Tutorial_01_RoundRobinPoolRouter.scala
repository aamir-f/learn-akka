package com.allaboutscala.learn.akka.routers

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor._
import akka.routing.{RoundRobinPool, DefaultResizer}
import akka.util.Timeout

import scala.concurrent.Future

object Tutorial_01_RoundRobinPoolRouter extends App {


  println("Step 1: Create an actor system")
  val system = ActorSystem("DonutStoreActorSystem")



  println("\nStep 5: Define DonutStockActor")
  val donutStockActor = system.actorOf(Props[DonutStockActor], name = "DonutStockActor")



  println("\nStep 6: Use Akka Ask Pattern and send a bunch of requests to DonutStockActor")
  import DonutStoreProtocol._
  import akka.pattern._
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  implicit val timeout = Timeout(5 second)

  val vanillaStockRequests = (1 to 10).map(i => (donutStockActor ? CheckStock("vanilla")).mapTo[Int])
  for {
    results <- Future.sequence(vanillaStockRequests)
  } yield println(s"vanilla stock results = $results")

  Thread.sleep(5000)



  val isTerminated = system.terminate()



  println("\nStep 2: Define the message passing protocol for our DonutStoreActor")
  object DonutStoreProtocol {
    case class Info(name: String)

    case class CheckStock(name: String)

    case class WorkerFailedException(error: String) extends Exception(error)
  }



  println("\nStep 3: Create DonutStockActor")
  class DonutStockActor extends Actor with ActorLogging {

    override def supervisorStrategy: SupervisorStrategy =
      OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 5 seconds) {
        case _: WorkerFailedException =>
          log.error("Worker failed exception, will restart.")
          Restart

        case _: Exception =>
          log.error("Worker failed, will need to escalate up the hierarchy")
          Escalate
      }

    // We will not create one worker actor.
    // val workerActor = context.actorOf(Props[DonutStockWorkerActor], name = "DonutStockWorkerActor")

    // We are using a resizable RoundRobinPool.
    val resizer = DefaultResizer(lowerBound = 5, upperBound = 10)
    val props = RoundRobinPool(5, Some(resizer), supervisorStrategy = supervisorStrategy)
      .props(Props[DonutStockWorkerActor])
    val donutStockWorkerRouterPool: ActorRef = context.actorOf(props, "DonutStockWorkerRouter")

    def receive = {
      case checkStock @ CheckStock(name) =>
        log.info(s"Checking stock for $name donut")
        donutStockWorkerRouterPool forward checkStock
    }
  }



  println("\ntep 4: Worker Actor called DonutStockWorkerActor")
  class DonutStockWorkerActor extends Actor with ActorLogging {

    override def postRestart(reason: Throwable): Unit = {
      log.info(s"restarting ${self.path.name} because of $reason")
    }

    def receive = {
      case CheckStock(name) =>
        sender ! findStock(name)
    }

    def findStock(name: String): Int = {
      log.info(s"Finding stock for donut = $name, thread = ${Thread.currentThread().getId}")
      100
    }
  }
}
