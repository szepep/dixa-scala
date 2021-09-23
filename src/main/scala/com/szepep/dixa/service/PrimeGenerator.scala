package com.szepep.dixa.service

import scala.collection.immutable

trait PrimeGenerator {

  def primesUntil(n: Int): immutable.Seq[Int]

}
