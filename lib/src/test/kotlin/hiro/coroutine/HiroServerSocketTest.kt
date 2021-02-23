package hiro.coroutine

import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.withTimeout
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.net.Socket
import java.time.Duration

/**
 * @author  : luoweiyao
 * @created : 2021/2/22
 * @project : hiro-gateway
 * @package : hiro.coroutine
 */
private val portSequence = generateSequence(10000) {
  it + 1
}.iterator()

internal class HiroServerSocketTest {
  private lateinit var stubServer: StubServer

  @BeforeEach
  internal fun setUp() {
    stubServer = StubServer(portSequence.next(), NioEventLoopGroup())
  }

  @Test
  fun `should return new HiroSocket when accept new connection`() {
    runBlocking {
      stubServer.connect()
      val result = stubServer.serverChannel.accept()
      assertThat(result).isNotNull()
    }
  }

  @Test
  fun `should queued connection when multiple connection arrives`() {
    runBlocking {
      stubServer.connect()
      stubServer.connect()
      stubServer.serverChannel.accept()
      delay(3000)
      val result = stubServer.serverChannel.accept()
      assertThat(result).isNotNull()
    }
  }

  @Test
  internal fun `should hang when no connection arrives`() {
    assertThatExceptionOfType(TimeoutCancellationException::class.java).isThrownBy {
      runBlocking {
        withTimeout(Duration.ofMillis(1000)) {
          stubServer.serverChannel.accept()
        }
      }
    }
  }

  private class StubServer(val port: Int, eventLoopGroup: EventLoopGroup) {
    val serverChannel = runBlocking {
      HiroServerSocket.listenOn(InetSocketAddress(port), eventLoopGroup)
    }

    fun connect(): Socket {
      return Socket("127.0.0.1", port)
    }
  }
}
