package com.szepep.dixa.service

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.szepep.dixa.proto.{PrimeService, Request, Response}


class PrimeServiceImpl(implicit generator: PrimeGenerator) extends PrimeService {

  override def get(in: Request): Source[Response, NotUsed] =
    Source(generator.primesUntil(in.number)).map(p => Response(p))

}

