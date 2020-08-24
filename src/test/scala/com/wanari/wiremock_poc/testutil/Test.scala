package com.wanari.wiremock_poc.testutil

import org.mockito.MockitoSugar
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}

trait Test extends AnyWordSpecLike with Matchers with MockitoSugar {
  def await[T](f: Future[T]): T = {
    Await.result(f, 5.seconds)
  }
}
