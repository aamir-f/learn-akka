package com.allaboutscala.learn.akka.actors

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor._
import akka.util.Timeout

import scala.concurrent.Future


object Tutorial_10_Error_Kernel_Supervision extends App {

  println("Step 1: Create an actor system")
  val system = ActorSystem("DonutStoreActorSystem")



  println("\nStep 5: Define DonutStockActor")
  val donutStockActor = system.actorOf(Props[DonutStockActor], name = "DonutStockActor")



  println("\nStep 6: Akka Ask Pattern")
  import DonutStoreProtocol._
  import akka.pattern._
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  implicit val timeout = Timeout(5 second)

  val vanillaDonutStock: Future[Int] = (donutStockActor ? CheckStock("vanilla")).mapTo[Int]
  for {
    found <- vanillaDonutStock
  } yield (println(s"Vanilla donut stock = $found"))

 // Thread.sleep(5000)



 /// println("\nStep 7: Close the actor system")
 // val isTerminated = system.terminate()




  println("\nStep 2: Define the message passing protocol for our DonutStoreActor")
  object DonutStoreProtocol {
    case class Info(name: String)

    case class CheckStock(name: String)

    case class WorkerFailedException(error: String) extends Exception(error)
  }


  println("\nStep 3: Create DonutStockActor")
  class DonutStockActor extends Actor with ActorLogging {

    override def supervisorStrategy: SupervisorStrategy =
      OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 seconds) {
        case _: WorkerFailedException =>
          log.error("Worker failed exception, will restart.")
          Restart

        case _: Exception =>
          log.error("Worker failed, will need to escalate up the hierarchy")
          Escalate
      }

    val workerActor = context.actorOf(Props[DonutStockWorkerActor], name = "DonutStockWorkerActor")

    def receive = {
      case checkStock @ CheckStock(name) =>
        log.info(s"Checking stock for $name donut")
        workerActor forward checkStock
    }
  }



  println("\nStep 4: Worker Actor called DonutStockWorkerActor")
  class DonutStockWorkerActor extends Actor with ActorLogging {

    override def preStart(): Unit = {
      log.info("====preStart======")
    }

    @throws[Exception](classOf[Exception])
    override def postRestart(reason: Throwable): Unit = {
      log.info(s"postRestart, restarting ${self.path.name} because of $reason")
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.info(s"preRestart, restarting ${self.path.name} because of $reason")
    }



    override def postStop(): Unit = {
      log.info("======postStop========")
    }

    def receive = {
      case CheckStock(name) =>
        findStock(name)
       context.stop(self)
    }

    def findStock(name: String): Int = {
      log.info(s"Finding stock for donut = $name")
      100
      //throw new IllegalStateException("boom") // Will Escalate the exception up the hierarchy
      throw WorkerFailedException("boom") // Will Restart DonutStockWorkerActor
    }
  }

}
