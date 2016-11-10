package io.prime.sse.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.ssl.SslContext;

public class SseClientInitializer extends ChannelInitializer<SocketChannel>
{
	private SslContext sslCtx;
	
	public SseClientInitializer(SslContext sslCtx) 
	{
		this.sslCtx = sslCtx;
	}
		
	@Override
	protected void initChannel(SocketChannel channel) throws Exception 
	{
		ChannelPipeline pipeline = channel.pipeline();
		
		if ( null != this.sslCtx ) {
			pipeline.addLast(sslCtx.newHandler(channel.alloc()));
		}
		
		pipeline.addLast(new HttpClientCodec());
		pipeline.addLast(new SseClientHandler());
	}
}
