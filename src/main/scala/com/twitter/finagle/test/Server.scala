package com.twitter.finagle.test

import java.net.InetSocketAddress

import org.jboss.netty.buffer._
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.http._

import com.twitter.finagle.builder._
import com.twitter.ostrich.{ServiceTracker, Service, Config, RuntimeEnvironment}

object ServerTest extends Service {
  class Handler extends SimpleChannelUpstreamHandler {
    override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
      val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
      response.setHeader("Content-Type", "text/plain")
      response.setContent(ChannelBuffers.wrappedBuffer("Mission accomplished".getBytes))
      e.getChannel.write(response).addListener(ChannelFutureListener.CLOSE)
    }
  }

  def main(args: Array[String]) {
    val config = new Config {
      def telnetPort = 0
      def httpBacklog = 0
      def httpPort = 8889
      def jmxPackage = None
    }

    ServiceTracker.register(this)
    ServiceTracker.startAdmin(config, new RuntimeEnvironment(getClass))

    val pf = new ChannelPipelineFactory {
      def getPipeline = {
        val pipeline = Channels.pipeline
        pipeline.addLast("handler", new Handler)
        pipeline
      }
    }
    val bs =
      ServerBuilder()
       .codec(Http)
       .reportTo(Ostrich())
       .pipelineFactory(pf)
       .build

    val addr = new InetSocketAddress(8888)
    println("HTTP demo running on %s".format(addr))
    bs.bind(addr)
  }

  def quiesce() = ()
  def shutdown() = ()
}
