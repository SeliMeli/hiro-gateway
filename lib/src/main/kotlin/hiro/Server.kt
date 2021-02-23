package hiro

import hiro.coroutine.HiroServerSocket
import hiro.coroutine.HiroSocket
import hiro.handler.DelayedEchoHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
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

  fun coBootsrap() = runBlocking {
    val serverSocket = HiroServerSocket.listenOn(InetSocketAddress(8081), NioEventLoopGroup())
    while(true) {
      launch {
        val socket = serverSocket.accept()
        handleSocket(socket)
      }
    }
  }

  private suspend fun handleSocket(socket: HiroSocket) {
    delay(200)
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
