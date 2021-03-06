package com.allaboutscala.learn.akka.http.routes

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshaller.identityUnmarshaller
import com.allaboutscala.learn.akka.http.jsonsupport.{Donut, Donuts, JsonSupport}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Created by Nadim Bahadoor on 28/06/2016.
  *
  *  Tutorial: Learn How To Use Akka HTTP
  *
  * [[http://allaboutscala.com/scala-frameworks/akka/]]
  *
  * Copyright 2016 Nadim Bahadoor (http://allaboutscala.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  *  [http://www.apache.org/licenses/LICENSE-2.0]
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */

final case class Ingredient(donutName: String, priceLevel: Double)

class DonutRoutes extends JsonSupport with LazyLogging {

  val donutDao = new DonutDao()

  def route(): Route = {
    path("create-donut") {
      post {
        entity(as[Donut]) { donut =>
          logger.info(s"creating donut = $donut")
          complete(StatusCodes.Created, s"Created donut = $donut")
        }
      } ~ delete {
        complete(StatusCodes.MethodNotAllowed, "The HTTP DELETE operation is not allowed for the create-donut path.")
      }
    } ~ path("donuts") {
      get {
        onSuccess(donutDao.fetchDonuts()) { donuts =>
          complete(StatusCodes.OK, donuts)
        }
      }
    } ~ path("donuts-with-future-success-failure") {
      get {
        onComplete(donutDao.fetchDonuts()) {
          case Success(donuts) => complete(StatusCodes.OK, donuts)
          case Failure(ex) => complete(s"Failed to fetch donuts = ${ex.getMessage}")
        }
      }
    } ~ path("complete-with-http-response") {
      get {
        complete(HttpResponse(status = StatusCodes.OK, entity = "Using an HttpResponse object"))
      }
    } ~ path("donut-with-try-httpresponse") {
      get {
        val result: HttpResponse = donutDao.tryFetchDonuts().getOrElse(donutDao.defaultResponse())
        complete(result)
      }
    } ~ path("akka-http-failwith") {
      get {
        failWith(new RuntimeException("Boom"))
      }
    } ~ path("akka-http-getresource") {
      getFromResource("error-page.html")
    } ~ path("donuts" / Segment) { donutName =>
      get {
        val result = donutDao.donutDetails(donutName)
        onSuccess(result) { donutDetail =>
          complete(StatusCodes.OK, donutDetail)
        }
      }
    } ~ path("donuts" / "stock" / new scala.util.matching.Regex("""donut_[a-zA-Z0-9\-]*""")) { donutId =>
      get {
        complete(StatusCodes.OK, s"Looking up donut stock by donutId = $donutId")
      }
    } ~ path("donut" / "prices") {
      get {
        parameter("donutName") { donutName =>
          val output = s"Received parameter: donutName=$donutName"
          complete(StatusCodes.OK, output)
        }
      }
    } ~ path("donut" / "bake") {
      get {
        parameters('donutName, 'topping ? "sprinkles") { (donutName, topping) =>
          val output = s"Received parameters: donutName=$donutName and topping=$topping"
          complete(StatusCodes.OK, output)
        }
      }
    } ~ path("ingredients") {
        get {
          parameters('donutName.as[String], 'priceLevel.as[Double]) { (donutName, priceLevel) =>
            val output = s"Received parameters: donutName=$donutName, priceLevel=$priceLevel"
            complete(StatusCodes.OK, output)
          }
        }
      } ~ path("bake-donuts") {
        get {
          import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers.CsvSeq
          parameter('ingredients.as(CsvSeq[String])) { ingredients =>
            val output = s"Received CSV parameter: ingredients=$ingredients"
            complete(StatusCodes.OK, output)
          }
        }
      } ~ path("ingredients-to-case-class") {
        get {
          parameters('donutName.as[String], 'priceLevel.as[Double]).as(Ingredient) { ingredient =>
            val output = s"Encoded query parameters into case class, ingredient: $ingredient"
            complete(StatusCodes.OK, output)
          }
        }
      } ~ path("request-with-headers") {
        get {
          extractRequest { httpRequest =>
            val headers = httpRequest.headers.mkString(", ")
            complete(StatusCodes.OK, s"headers = $headers")
          }
        }
      } ~ path("multiple-segments" / Segments ) { segments =>
        get {
          val partA :: partB :: partC :: Nil = segments
          val output =
            s"""
               |Received the following Segments = $segments, with
               |partA = $partA
               |partB = $partB
               |partC = $partC
             """.stripMargin
          complete(StatusCodes.OK, output)
        }
      }
    }
}


class DonutDao {
  import scala.concurrent.ExecutionContext.Implicits.global

  val donutsFromDb = Vector(
    Donut("Plain Donut", 1.50),
    Donut("Chocolate Donut", 2),
    Donut("Glazed Donut", 2.50)
  )

  def fetchDonuts(): Future[Donuts] = Future {
    Donuts(donutsFromDb)
  }

  def tryFetchDonuts(): Try[HttpResponse] = Try {
    throw new IllegalStateException("Boom!")
  }

  def defaultResponse(): HttpResponse =
    HttpResponse(
      status = StatusCodes.NotFound,
      entity = "An unexpected error occurred. Please try again.")

  def donutDetails(donutName: String): Future[String] = Future {
    // this is obviously not as efficient as we're scanning a Vector by just one property of the Donut domain object.
    val someDonut = donutsFromDb.find(_.name == donutName)
    someDonut match {
      case Some(donut) => s"$donut"
      case None => s"Donut = $donutName was not found."
    }
  }
}