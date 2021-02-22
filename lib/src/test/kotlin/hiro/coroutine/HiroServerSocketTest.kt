package hiro.coroutine

import io.netty.channel.nio.NioEventLoopGroup
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertTimeout
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.concurrent.CompletableFuture

/**
 * @author  : luoweiyao
 * @created : 2021/2/22
 * @project : hiro-gateway
 * @package : hiro.coroutine
 */
internal class HiroServerSocketTest {
  private val hiroServerSocket = runBlocking {
    HiroServerSocket.listenOn(InetSocketAddress(10801), NioEventLoopGroup())
  }

  @Test
  fun `should return new HiroSocket when accept new connection`() {
    runBlocking {
      connectToServerSocket()
      val result = hiroServerSocket.accept()
      assertThat(result).isNotNull()
    }
  }

  @Test
  fun `should hanging when old socket not consumed`() {
    //TODO: fix
    val a = CompletableFuture.runAsync {
      connectToServerSocket()
    }
    val b = CompletableFuture.runAsync {
      connectToServerSocket()
    }
    CompletableFuture.allOf(a, b).join()
  }

  private fun connectToServerSocket() {
    Socket().connect(InetSocketAddress(10801), 10)
  }
}
