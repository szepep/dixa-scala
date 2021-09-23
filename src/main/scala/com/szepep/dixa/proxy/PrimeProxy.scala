package com.szepep.dixa.proxy

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.GrpcClientSettings
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model.{ContentTypeRange, ContentTypes, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import com.szepep.dixa.proto.{PrimeServiceClient, Request, Response}

object PrimeProxy {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "PrimeProxy")
    implicit val executionContext = system.executionContext

    implicit val marshaller = Marshaller.strict[Response, ByteString] { resp =>
      Marshalling.WithFixedContentType(ContentTypes.`text/plain(UTF-8)`, () => {
        ByteString(resp.prime.toString)
      })
    }

    implicit val streamingSupport =
      EntityStreamingSupport.csv
        .withSupported(ContentTypeRange(ContentTypes.`text/plain(UTF-8)`))
        .withContentType(ContentTypes.`text/plain(UTF-8)`)
        .withFramingRenderer(
          Flow[ByteString].zipWithIndex.map {
            case (bs, 0) => bs
            case (bs, _) => ByteString(",") ++ bs
          })

    val client = PrimeServiceClient(GrpcClientSettings.fromConfig("Dixa.PrimeService"))

    val route =
      pathPrefix("prime" / IntNumber) { number =>
        get {
          if (number < 0) complete(HttpResponse(StatusCodes.BadRequest, entity = "The number must zero or positive\n"))
          else complete {
            client.get(Request(number))
          }
        }
      }

    Http().newServerAt("localhost", 8080).bind(route)
  }
}