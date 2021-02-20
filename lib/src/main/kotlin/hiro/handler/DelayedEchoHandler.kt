package hiro.handler

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.charset.Charset

class DelayedEchoHandler: ChannelInboundHandlerAdapter() {
  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
    val buff = ctx.channel().alloc().directBuffer()
    buff.writeCharSequence("hello", Charset.defaultCharset())
    val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buff)
    GlobalScope.launch {
      delay(200)
      ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
    }
  }
}
