package com.szepep.dixa.proxy

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.GrpcClientSettings
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model.{ContentTypeRange, ContentTypes}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import com.szepep.dixa.proto.{PrimeServiceClient, Request, Response}

import scala.io.StdIn

object Proxy {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "PrimeProxy")
    implicit val executionContext = system.executionContext

    implicit val marshaller = Marshaller.strict[Response, ByteString] { resp =>
      Marshalling.WithFixedContentType(ContentTypes.`text/plain(UTF-8)`, () => {
        ByteString(resp.prime.toString)
      })
    }

    implicit val streamingSupport =
      EntityStreamingSupport.csv(maxLineLength = 16 * 1024)
        .withSupported(ContentTypeRange(ContentTypes.`text/plain(UTF-8)`))
        .withContentType(ContentTypes.`text/plain(UTF-8)`)
        .withFramingRenderer(Flow[ByteString].map(bs => bs ++ ByteString(",")))

    val client = PrimeServiceClient(GrpcClientSettings.fromConfig("Dixa.PrimeService"))

    val route =
      pathPrefix("prime" / IntNumber) { number =>
        get {
          complete { client.get(Request(number)) }
        }
      }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    println(s"Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }


}