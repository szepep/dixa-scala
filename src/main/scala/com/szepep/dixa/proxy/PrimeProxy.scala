package com.szepep.dixa.proxy

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.GrpcClientSettings
import com.szepep.dixa.proto.{PrimeServiceClient, Request}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object PrimeProxy {

  def main(args: Array[String]): Unit = {
    implicit val sys: ActorSystem[_] = ActorSystem(Behaviors.empty, "PrimeProxy")
    implicit val ec: ExecutionContext = sys.executionContext

    val client = PrimeServiceClient(GrpcClientSettings.fromConfig("Dixa.PrimeService"))

    val names = List(10, 100)

    names.foreach(singleRequestReply)

    def singleRequestReply(number: Int): Unit = {
      val responseStream = client.get(Request(number))
      val done: Future[Done] =
        responseStream.runForeach(reply => println(s"$number got streaming reply: ${reply.prime}"))

      done.onComplete {
        case Success(_) =>
          println("streamingBroadcast done")
        case Failure(e) =>
          println(s"Error streamingBroadcast: $e")
      }
    }
  }

}
