package com.szepep.dixa.proto

import akka.actor.ActorSystem
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.testkit.TestKit
import akka.stream.scaladsl.Sink
import com.szepep.dixa.service.{PrimeGenerator, PrimeServiceImpl, SimplePrimeGenerator}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._

class PrimeServiceImplSpec
  extends TestKit(ActorSystem("MySpec"))
    with AnyWordSpecLike
    with BeforeAndAfterAll
    with Matchers
    with ScalaFutures {

  val testKit = ActorTestKit()

  implicit val patience: PatienceConfig = PatienceConfig(scaled(5.seconds), scaled(100.millis))
  implicit val generator: PrimeGenerator = SimplePrimeGenerator
  val service = new PrimeServiceImpl

  override def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }

  "PrimeServiceImpl" should {

    "primes until 0" in {
      primesUntil(0) should be(empty)
    }

    "primes until 2" in {
      primesUntil(2) should be(Seq(2))
    }

    "primes until 10" in {
      primesUntil(10) should be(Seq(2, 3, 5, 7))
    }

    "primes until 11" in {
      primesUntil(11) should be(Seq(2, 3, 5, 7, 11))
    }

    def primesUntil(number: Int): Seq[Int] = {
      val reply = service.get(Request(number))
      val future = reply.map(_.prime).runWith(Sink.seq)
      Await.result(future, 3.seconds)
    }

  }
}
