package hiro.coroutine

import hiro.coAwait
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.channels.Channel as KChannel
import java.net.SocketAddress


/**
 * @author  : luoweiyao
 * @created : 2021/2/22
 * @project : hiro-gateway
 * @package : hiro.coroutine
 */
class HiroServerSocket {
  private val coroutineChannel = KChannel<HiroSocket>()

  suspend fun accept(): HiroSocket = coroutineChannel.receive()

  companion object {
    suspend fun listenOn(inetSocketAddress: SocketAddress, bossEventLoop: EventLoopGroup): HiroServerSocket {
      val hiroServerSocket = HiroServerSocket()
      when(bossEventLoop) {
        is NioEventLoopGroup -> listenOnNio(inetSocketAddress, bossEventLoop, hiroServerSocket)
        else -> throw NotImplementedError()
      }
      return hiroServerSocket
    }

    private suspend fun listenOnNio(
      inetSocketAddress: SocketAddress,
      bossEventLoop: NioEventLoopGroup,
      hiroServerSocket: HiroServerSocket
    ) {
      val nettyServerSocketChannel = NioServerSocketChannel()
      nettyServerSocketChannel.pipeline().addLast(HiroServerSocketAcceptor(hiroServerSocket.coroutineChannel))
      bossEventLoop.register(nettyServerSocketChannel).coAwait()
      nettyServerSocketChannel.bind(inetSocketAddress).coAwait()
    }
  }
}

private class HiroServerSocketAcceptor(private val coroutineChannel: KChannel<HiroSocket>): ChannelInboundHandlerAdapter() {
  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    coroutineChannel.sendBlocking(HiroSocket())
  }
}
