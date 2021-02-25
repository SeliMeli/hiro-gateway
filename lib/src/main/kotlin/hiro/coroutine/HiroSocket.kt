package hiro.coroutine

import hiro.coAwait
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.EventLoopGroup
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext

class HiroSocket private constructor(private val channel: Channel){
  companion object {
    suspend fun bindChannel(channel: Channel): HiroSocket {
      val retVal = HiroSocket(channel)
      channel.pipeline().addLast(HiroSocketInboundHandler())
      getCurrentEventLoopGroup().register(channel).coAwait()
      return retVal
    }

    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun getCurrentEventLoopGroup(): EventLoopGroup {
      val coroutineDispatcher = currentCoroutineContext()[CoroutineDispatcher]
      if (coroutineDispatcher !is NettyEventLoopGroupDispatcher) {
        throw UnsupportedDispatcherError(coroutineDispatcher)
      }
      return coroutineDispatcher.eventLoopGroup
    }
  }
}

private class HiroSocketInboundHandler(): ChannelInboundHandlerAdapter(
  //TODO: handle channel read
)
