package com.allaboutscala.learn.akka.actors

import akka.actor.{ActorSystem, Terminated}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Tutorial_01_ActorSystem_Introduction extends App  {

  println("Step 1: create an actor system")
  val system = ActorSystem("DonutStoreActorSystem")



  println("\nStep 2: close the actor system")
  val isTerminated: Future[Terminated] = system.terminate()



  println("\nStep 3: Check the status of the actor system")
  isTerminated.onComplete {
    case Success(result) => println(s"Successfully terminated actor system, ${result.getAddressTerminated}")
    case Failure(e)     => println(s"Failed to terminate actor system, ${e.getCause}")
  }
  Thread.sleep(5000)
}
