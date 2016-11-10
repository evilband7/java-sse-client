package io.prime.sse.client;

import java.net.URI;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class SseClientBuilder  implements GenericFutureListener<Future<Void>>
{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

//	@Value("${event.sse.url}")
	private URI uri;
    
    private EventLoopGroup group;
    
    private Channel channel;
    
    @PreDestroy
    public void preDestroy()
    {
    	this.logger.info("Shutting down Gateway Http Server (Netty) channel");
    	this.channel.close();
    }
    
    private HttpRequest createRequest(String lastEventId)
    {
    	HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, this.uri.toString());
		String hostHeader = this.uri.getHost();
		String portString = "";
		
		if (80==this.uri.getPort() && this.uri.getScheme().equals("http") ) {
			// do nothing
		} else if (443==this.uri.getPort() && this.uri.getScheme().equals("https")) {
			// do nothing
		} else {
			portString = ":" + this.uri.getPort();
		}
		
    	request.headers()
			.add(HttpHeaderNames.ACCEPT, "text/event-stream")
			.add(HttpHeaderNames.HOST, hostHeader + portString)
			.add(HttpHeaderNames.ORIGIN, this.uri.getScheme() + "://" + this.uri.getHost() + portString)
			.add(HttpHeaderNames.CACHE_CONTROL, "no-cache")
			;
    	
    	if (false == StringUtils.isEmpty(lastEventId)) {
    		request.headers().add("Last-Event-ID", lastEventId);
    	}
    	return request;
    }
    
    @PostConstruct
    public void createChannel() throws Exception 
    {
    	this.logger.info("Starting Gateway Http Server (Netty)");
        final SslContext sslCtx;
        
        if ("https".equals(this.uri.getScheme())) {
            sslCtx = SslContextBuilder.forClient().build();
        } else {
            sslCtx = null;
        }
        
        group = new NioEventLoopGroup(1);
		Bootstrap b = new Bootstrap();
    	b.group(group)
    		.channel(NioSocketChannel.class)
    		.handler(new SseClientInitializer(sslCtx))
    		;
    	
    	HttpRequest request = this.createRequest(null);
    	channel = b.connect(this.uri.getHost(), this.uri.getPort()).sync().channel();
        this.channel.closeFuture().addListener(this);
        this.channel
        	.writeAndFlush(request)
	        .addListener(new ChannelFutureListener()
		    {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					System.out.println("$$$$ OPERATION_COMPLETE: " + future.isSuccess());
				}
				
		    });
    }

	@Override
	public void operationComplete(Future<Void> future) throws Exception {
		this.logger.info("Shutting down workgroup of Gateway Http Server (Netty) gracefully");
		this.group.shutdownGracefully();
	}
	
	public static void main(String[] args) throws Exception
	{
		SseClientBuilder client = new SseClientBuilder();
		client.uri = new URI("http://localhost:28080/map/internal/event/sse");
		client.createChannel();
	}
	
}
