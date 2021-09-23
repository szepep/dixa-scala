package com.szepep.dixa.service

import scala.collection.compat.immutable.LazyList
import scala.collection.immutable

object SimplePrimeGenerator extends PrimeGenerator {

  private val primes: LazyList[Int] = 2 #:: LazyList.from(3, 2)
    .filter { n => !primes.takeWhile(_ <= math.sqrt(n)).exists(n % _ == 0) }

  override def primesUntil(n: Int): immutable.Seq[Int] = primes.takeWhile(_ <= n)
}
