package hiro.coroutine

import hiro.coAwait
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.EventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.currentCoroutineContext
import java.net.SocketAddress
import io.netty.channel.Channel as NettyChannel
import kotlinx.coroutines.channels.Channel as KChannel

class HiroServerSocket private constructor() {
  private val nettyChannelCache = KChannel<NettyChannel>()

  suspend fun accept(): HiroSocket {
    val uninitializedNettyChannel = nettyChannelCache.receive()
    // TODO(add handler to channel)
    uninitializedNettyChannel.pipeline().addLast()
    val registerFuture = getCurrentEventLoopGroup().register(uninitializedNettyChannel)
    registerFuture.coAwait()
    return HiroSocket(registerFuture.channel())
  }

  @OptIn(ExperimentalStdlibApi::class)
  private suspend fun getCurrentEventLoopGroup(): EventLoopGroup {
    val coroutineDispatcher = currentCoroutineContext()[CoroutineDispatcher]
    if (coroutineDispatcher !is NettyEventLoopGroupDispatcher) {
      throw UnsupportedDispatcherError(coroutineDispatcher)
    }
    return coroutineDispatcher.eventLoopGroup
  }

  companion object {
    suspend fun listenOn(inetSocketAddress: SocketAddress, bossEventLoop: EventLoopGroup): HiroServerSocket {
      val hiroServerSocket = HiroServerSocket()
      when (bossEventLoop) {
        is NioEventLoopGroup -> listenOnNio(inetSocketAddress, bossEventLoop, hiroServerSocket)
        is KQueueEventLoopGroup -> listenOnKQueue(inetSocketAddress, bossEventLoop, hiroServerSocket)
        is EpollEventLoopGroup -> listenOnEpoll(inetSocketAddress, bossEventLoop, hiroServerSocket)
        else -> throw NotImplementedError()
      }
      return hiroServerSocket
    }

    private suspend fun listenOnEpoll(
      inetSocketAddress: SocketAddress,
      bossEventLoop: EpollEventLoopGroup,
      hiroServerSocket: HiroServerSocket
    ) {
      val serverChannel = EpollServerSocketChannel()
      initServerChannel(serverChannel, hiroServerSocket, bossEventLoop, inetSocketAddress)
    }

    private suspend fun listenOnKQueue(
      inetSocketAddress: SocketAddress,
      bossEventLoop: KQueueEventLoopGroup,
      hiroServerSocket: HiroServerSocket
    ) {
      val serverChannel = KQueueServerSocketChannel()
      initServerChannel(serverChannel, hiroServerSocket, bossEventLoop, inetSocketAddress)
    }

    private suspend fun listenOnNio(
      inetSocketAddress: SocketAddress,
      bossEventLoop: NioEventLoopGroup,
      hiroServerSocket: HiroServerSocket
    ) {
      val nettyServerSocketChannel = NioServerSocketChannel()
      initServerChannel(nettyServerSocketChannel, hiroServerSocket, bossEventLoop, inetSocketAddress)
    }

    private suspend fun initServerChannel(
      nettyServerSocketChannel: ServerChannel,
      hiroServerSocket: HiroServerSocket,
      bossEventLoop: EventLoopGroup,
      inetSocketAddress: SocketAddress
    ) {
      nettyServerSocketChannel.pipeline()
        .addLast(HiroServerSocketAcceptor(hiroServerSocket.nettyChannelCache))
      bossEventLoop.register(nettyServerSocketChannel).coAwait()
      nettyServerSocketChannel.bind(inetSocketAddress).coAwait()
    }
  }
}

private class HiroServerSocketAcceptor(private val nettyChannelCache: KChannel<NettyChannel>) :
  ChannelInboundHandlerAdapter() {
  override fun channelRead(ctx: ChannelHandlerContext, msg: Any): Unit =
    nettyChannelCache.sendBlocking(msg as NettyChannel)
}
