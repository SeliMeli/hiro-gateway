package hiro

import hiro.handler.DelayedEchoHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInboundHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpRequestEncoder
import io.netty.handler.codec.http.HttpResponseEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.SocketAddress
import kotlin.coroutines.suspendCoroutine


class Server {
  fun bootstrap() {
    val b = ServerBootstrap()
    val worker = KQueueEventLoopGroup(6)
    val boss = KQueueEventLoopGroup(2)
    try {
      val f = b.group(boss, worker)
        .channel(KQueueServerSocketChannel::class.java)
        .handler(LoggingHandler(LogLevel.DEBUG))
        .childHandler(object : ChannelInitializer<SocketChannel>() {
          override fun initChannel(ch: SocketChannel) {
            ch.pipeline().addLast(HttpRequestDecoder(), HttpResponseEncoder(), DelayedEchoHandler())
          }
        })
        .localAddress(10920)
        .bind().sync()
      f.channel().closeFuture().sync()
    } finally {
      boss.shutdownGracefully()
      worker.shutdownGracefully()
    }
  }
}

fun main(args: Array<String>) {
  Server().bootstrap()
}

suspend fun ChannelFuture.coAwait() = suspendCoroutine<Any?> {
  this.addListener {
    _ -> it.resumeWith(Result.success(null))
  }
}
