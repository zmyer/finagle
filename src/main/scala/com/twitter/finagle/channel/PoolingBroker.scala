package com.twitter.finagle.channel

import org.jboss.netty.channel._

class PoolingBroker(channelPool: ChannelPool) extends ConnectingChannelBroker {
  def getChannel = channelPool.reserve()
  def putChannel(channel: Channel) = channelPool.release(channel)
}
