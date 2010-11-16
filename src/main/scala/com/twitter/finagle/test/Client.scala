package com.twitter.finagle.test

import org.jboss.netty.handler.codec.http._

import com.twitter.ostrich
import com.twitter.finagle.builder.{ClientBuilder, Http, Ostrich}
import com.twitter.finagle.stub.Stub
import com.twitter.ostrich.{Config, RuntimeEnvironment}

object ClientTest extends ostrich.Service {
  def main(args: Array[String]) {
    val config = new Config {
      def telnetPort = 0
      def httpBacklog = 0
      def httpPort = 8889
      def jmxPackage = null
    }
    ostrich.ServiceTracker.register(this)
    ostrich.ServiceTracker.startAdmin(config, new RuntimeEnvironment(getClass))

    val client =
      ClientBuilder()
        .name("http")
        .hosts("localhost:10000,localhost:10001,localhost:10003")
        .codec(Http)
        .exportLoadsToOstrich()
        .reportTo(Ostrich())
        .buildStub[HttpRequest, HttpResponse]()

    for (_ <- 0 until 100)
      makeRequest(client)
  }

  def makeRequest(client: Stub[HttpRequest, HttpResponse]) {
    client(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")) respond {
      case _ =>
        makeRequest(client)
    }
  }

  def quiesce() = ()
  def shutdown() = ()
}
