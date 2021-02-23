package hiro.coroutine

import io.netty.channel.EventLoopGroup
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

class NettyEventLoopGroupDispatcher(private val eventLoopGroup: EventLoopGroup): CoroutineDispatcher() {
  override fun dispatch(context: CoroutineContext, block: Runnable) {
    eventLoopGroup.execute(block)
  }
}
